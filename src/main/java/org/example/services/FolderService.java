package org.example.services;

import org.example.domain.model.*;
import org.example.domain.repository.*;
import org.example.dto.response.FolderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FolderService {

    private final FolderRepository folderRepository;
    private final FolderHierarchyRepository folderHierarchyRepository;
    private final FileRepository fileRepository;

    @Autowired
    public FolderService(FolderRepository folderRepository,
                         FolderHierarchyRepository folderHierarchyRepository,
                         FileRepository fileRepository) {
        this.folderRepository = folderRepository;
        this.folderHierarchyRepository = folderHierarchyRepository;
        this.fileRepository = fileRepository;
    }

    @Transactional(readOnly = true)
    public List<FolderResponse> getAllFolders() {
        return folderRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FolderResponse createFolder(String path) throws IOException {
        path = path.replaceAll("^/+|/+$", "");
        List<String> segments = Arrays.asList(path.split("/"));

        if (segments.isEmpty()) {
            throw new IllegalArgumentException("Invalid path");
        }

        List<String> parentSegments = segments.subList(0, segments.size() - 1);
        String folderName = segments.get(segments.size() - 1);

        // Start traversal from root folder
        Folder parent = getRootFolder(); // Default to root if no parent segments
        if (!parentSegments.isEmpty()) {
            parent = traverseFolderHierarchy(parentSegments);
        }

        // Check for existing folder
        if (folderRepository.existsByNameAndParent(folderName, parent)) {
            throw new IllegalArgumentException("Folder already exists in this location");
        }

        // Create and save the new folder
        Folder folder = new Folder();
        folder.setName(folderName);
        folder.setParent(parent);
        folder.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        folder.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        Folder savedFolder = folderRepository.save(folder);

        updateFolderHierarchy(savedFolder);
        createFolderOnFilesystem(savedFolder);

        return mapToResponse(savedFolder);
    }

    private Folder traverseFolderHierarchy(List<String> pathSegments) {
        Folder current = getRootFolder(); // Start from root
        for (String segment : pathSegments) {
            current = folderRepository.findByNameAndParent(segment, current)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Parent folder path '" + segment + "' does not exist. Create parent folders first."
                    ));
        }
        return current;
    }

    private Folder getRootFolder() {
        return folderRepository.findByNameAndParent("root", null)
                .orElseThrow(() -> new IllegalStateException("Root folder not found"));
    }

    @Transactional
    public FolderResponse renameFolder(UUID folderId, String newName) throws IOException {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found"));

        if (folder.getName().equals(newName)) {
            return mapToResponse(folder);
        }

        if (folderRepository.existsByNameAndParent(newName, folder.getParent())) {
            throw new IllegalArgumentException("Folder name already exists in parent");
        }

        Path oldPath = buildFolderPath(folder);
        folder.setName(newName);
        folder.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        Folder updatedFolder = folderRepository.save(folder);

        updateAllDescendantPaths(updatedFolder);
        renameFolderOnFilesystem(oldPath, updatedFolder);

        return mapToResponse(updatedFolder);
    }

    @Transactional
    public void deleteFolder(UUID folderId) throws IOException {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found"));

        // 1. Get all descendant folders including self
        List<UUID> allFolderIds = folderHierarchyRepository.findDescendantIds(folderId);
        allFolderIds.add(folderId); // Include parent folder

        // 2. Delete files first
        fileRepository.deleteAllByFolderIdIn(allFolderIds);

        // 3. Delete hierarchy relationships
        folderHierarchyRepository.deleteByFolderIdIn(allFolderIds);
        folderHierarchyRepository.deleteByAncestorIdIn(allFolderIds);

        // 4. Delete folders in reverse depth order
        List<Folder> foldersToDelete = folderRepository.findAllById(allFolderIds);
        foldersToDelete.sort(Comparator.comparingInt(this::getFolderDepth).reversed());
        folderRepository.deleteAll(foldersToDelete);

        // 5. Filesystem cleanup
        deleteFolderStructureFromFilesystem(folder);
    }

    // Get folder depth using hierarchy data
    private int getFolderDepth(Folder folder) {
        return folderHierarchyRepository.findByFolderId(folder.getId())
                .stream()
                .mapToInt(FolderHierarchy::getDepth)
                .max()
                .orElse(0);
    }

    private void deleteFolderStructureFromFilesystem(Folder folder) throws IOException {
        Path dirPath = buildDirectoryPath(folder);
        if (Files.exists(dirPath)) {
            Files.walk(dirPath)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to delete: " + path, e);
                        }
                    });
        }
    }

    private Path buildDirectoryPath(Folder folder) {
        Path basePath = Paths.get("public").toAbsolutePath().normalize();
        List<String> pathSegments = new ArrayList<>();

        Folder current = folder;
        while (current != null && !isRootFolder(current)) {
            pathSegments.add(current.getName());
            current = current.getParent();
        }

        Collections.reverse(pathSegments);
        for (String segment : pathSegments) {
            basePath = basePath.resolve(segment);
        }

        return basePath;
    }

    private boolean isRootFolder(Folder folder) {
        return "root".equals(folder.getName()) && folder.getParent() == null;
    }


    @Transactional(readOnly = true)
    public List<FolderResponse> getFoldersByParent(UUID parentId) {
        Folder parent = parentId != null
                ? folderRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent not found"))
                : null;

        return folderRepository.findByParent(parent)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private Folder ensureFolderStructure(List<String> pathSegments) {
        Folder currentParent = null;

        for (String segment : pathSegments) {
            final Folder finalCurrentParent = currentParent;

            currentParent = folderRepository.findByNameAndParent(segment, finalCurrentParent)
                    .orElseGet(() -> {
                        Folder newFolder = new Folder();
                        newFolder.setName(segment);
                        newFolder.setParent(finalCurrentParent);
                        newFolder.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                        newFolder.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
                        folderRepository.save(newFolder);
                        updateFolderHierarchy(newFolder);
                        return newFolder;
                    });
        }

        if (currentParent == null) {
            currentParent = folderRepository.findByNameAndParent("root", null)
                    .orElseThrow(() -> new IllegalStateException("Root folder not found"));
        }
        return currentParent;
    }

    private void updateFolderHierarchy(Folder folder) {
        if (folder.getParent() != null) {
            List<FolderHierarchy> parentHierarchy = folderHierarchyRepository
                    .findByAncestorId(folder.getParent().getId());

            for (FolderHierarchy hierarchy : parentHierarchy) {
                FolderHierarchy newHierarchy = new FolderHierarchy(
                        folder,
                        hierarchy.getAncestor(),
                        hierarchy.getDepth() + 1
                );
                folderHierarchyRepository.save(newHierarchy);
            }
        }

        // Add self-reference
        folderHierarchyRepository.save(new FolderHierarchy(folder, folder, 0));
    }

    private void updateAllDescendantPaths(Folder folder) {
        List<Folder> descendants = folderHierarchyRepository.findDescendants(folder.getId())
                .stream()
                .map(FolderHierarchy::getFolder)
                .collect(Collectors.toList());

        descendants.forEach(descendant -> {
            descendant.setPath(constructFolderPath(descendant));
            folderRepository.save(descendant);
        });
    }

    private String constructFolderPath(Folder folder) {
        List<String> pathSegments = new ArrayList<>();
        while (folder != null && !folder.getName().equals("root")) {
            pathSegments.add(folder.getName());
            folder = folder.getParent();
        }
        Collections.reverse(pathSegments);
        return String.join("/", pathSegments);
    }

    private Path buildFolderPath(Folder folder) {
        List<String> pathSegments = new ArrayList<>();
        Folder current = folder;
        while (current != null && !current.getName().equals("root")) {
            pathSegments.add(current.getName());
            current = current.getParent();
        }
        Collections.reverse(pathSegments);
        return Paths.get("public").resolve(String.join("/", pathSegments));
    }

    private void createFolderOnFilesystem(Folder folder) throws IOException {
        Path dirPath = buildFolderPath(folder);
        Files.createDirectories(dirPath);
    }

    private void renameFolderOnFilesystem(Path oldPath, Folder newFolder) throws IOException {
        Path newPath = buildFolderPath(newFolder);
        Files.move(oldPath, newPath);
    }

//    private void deleteFolderFromFilesystem(Folder folder) throws IOException {
//        Path dirPath = buildFolderPath(folder);
//        Files.walk(dirPath)
//                .sorted(Comparator.reverseOrder())
//                .forEach(path -> {
//                    try {
//                        Files.deleteIfExists(path);
//                    } catch (IOException e) {
//                        throw new RuntimeException("Failed to delete file: " + path, e);
//                    }
//                });
//    }

    private FolderResponse mapToResponse(Folder folder) {
        FolderResponse response = new FolderResponse();
        response.setId(folder.getId());
        response.setName(folder.getName());
        response.setPath(constructFolderPath(folder));
        response.setParentId(folder.getParent() != null ?
                folder.getParent().getId().toString() : null);
        response.setCreatedAt(folder.getCreatedAt());
        response.setUpdatedAt(folder.getUpdatedAt());
        return response;
    }
}