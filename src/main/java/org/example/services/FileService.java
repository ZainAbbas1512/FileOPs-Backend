package org.example.services;

import org.example.domain.model.*;
import org.example.domain.repository.FolderRepository;
import org.example.domain.repository.FileTypeRepository;
import org.example.domain.repository.FileRepository;
import org.example.domain.repository.FolderHierarchyRepository;

import org.example.dto.request.CreateFileRequest;
import org.example.dto.request.DeleteFileRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.*;

@Service
public class FileService {
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final FileTypeRepository fileTypeRepository;
    private final FolderHierarchyRepository folderHierarchyRepository;

    @Autowired
    public FileService(FileRepository fileRepository,
                       FolderRepository folderRepository,
                       FileTypeRepository fileTypeRepository,
                       FolderHierarchyRepository folderHierarchyRepository) {
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
        this.fileTypeRepository = fileTypeRepository;
        this.folderHierarchyRepository = folderHierarchyRepository;
    }

    @Transactional
    public List<FileMetadata> findAll() {
        return fileRepository.findAll();
    }

    @Transactional
    public FileMetadata createFile(CreateFileRequest request) throws IOException {
        // Handle folder path preprocessing
        String folderPath = request.getFolderPath() != null
                ? request.getFolderPath().trim().replaceAll("^/+|/+$", "")
                : "";

        List<String> pathSegments = folderPath.isEmpty()
                ? Collections.emptyList()
                : Arrays.asList(folderPath.split("/"));

        // Get or create folder structure
        Folder parentFolder = ensureFolderStructure(pathSegments);

        // Validate file type
        FileType fileType = fileTypeRepository.findByType(request.getFileType())
                .orElseThrow(() -> new IllegalArgumentException("Invalid file type"));

        // Check for duplicate name + type in same folder
        if (fileRepository.existsByNameAndFolderAndFileType(
                request.getName(),
                parentFolder,
                fileType
        )) {
            throw new IllegalArgumentException(
                    "File '" + request.getName() + "." + fileType.getType() +
                            "' already exists in this folder"
            );
        }

        // Create file entity
        FileMetadata file = new FileMetadata();
        file.setName(request.getName());
        file.setSize(request.getSize());
        file.setFolder(parentFolder);
        file.setData(Base64.getDecoder().decode(request.getData()));
        file.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        file.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        file.setFileType(fileType);  // Use the already retrieved fileType

        // Build path: root/folder1/folder2/filename
        String computedPath = constructFilePath(parentFolder) + "/" + request.getName();
        file.setPath(computedPath);

        // Persist and save
        FileMetadata savedFile = fileRepository.save(file);
        saveToDisk(parentFolder, savedFile);

        return savedFile;
    }

    @Transactional
    public void deleteFile(DeleteFileRequest request) {
        // Normalize path (remove leading/trailing slashes)
        String fullPath = request.getFullPath().replaceAll("^/+|/+$", "");

        // Validate file type
        FileType fileType = fileTypeRepository.findByType(request.getFileType())
                .orElseThrow(() -> new IllegalArgumentException("Invalid file type"));

        // Find file by path and type
        FileMetadata file = fileRepository.findByPathAndFileType(fullPath, fileType)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        // Delete from database
        fileRepository.delete(file);

        // Delete from filesystem
        deleteFromDisk(file);
    }

    private void deleteFromDisk(FileMetadata file) {
        try {
            Path filePath = Paths.get("public")
                    .resolve(file.getPath() + "." + file.getFileType().getType());
            System.out.println(filePath );

            Path rootPath = Paths.get("public").resolve("root");
            if (filePath.startsWith(rootPath)) {
                filePath = filePath.subpath(rootPath.getNameCount(), filePath.getNameCount());
                filePath = Paths.get("public").resolve(filePath);

                System.out.println("File path after removing 'root': " + filePath);
            }
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file from filesystem", e);
        }
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

    private void updateFolderHierarchy(Folder newFolder) {
        if (newFolder.getParent() != null) {
            // Add parent relationships
            List<FolderHierarchy> parentHierarchy = folderHierarchyRepository.findByFolderId(newFolder.getParent().getId());
            for (FolderHierarchy hierarchy : parentHierarchy) {
                FolderHierarchy newHierarchy = new FolderHierarchy(
                        newFolder,
                        hierarchy.getAncestor(),
                        hierarchy.getDepth() + 1
                );
                folderHierarchyRepository.save(newHierarchy);
            }
        }

        // Add self reference
        FolderHierarchy selfHierarchy = new FolderHierarchy(
                newFolder,
                newFolder,
                0
        );
        folderHierarchyRepository.save(selfHierarchy);
    }

    private String constructFilePath(Folder folder) {
        List<String> pathSegments = new ArrayList<>();
        while (folder != null) {
            pathSegments.add(folder.getName());
            folder = folder.getParent();
        }
        Collections.reverse(pathSegments);
        return String.join("/", pathSegments);
    }

    private void saveToDisk(Folder parentFolder, FileMetadata file) throws IOException {
        Path rootDir = Paths.get("public").toAbsolutePath().normalize();
        Files.createDirectories(rootDir);

        // Skip "root" folder name in filesystem path
        List<String> folderNames = new ArrayList<>();
        Folder current = parentFolder;
        while (current != null && !isRootFolder(current)) {
            folderNames.add(current.getName());
            current = current.getParent();
        }
        Collections.reverse(folderNames);

        Path storagePath = rootDir;
        for (String folderName : folderNames) {
            storagePath = storagePath.resolve(folderName);
        }

        Files.createDirectories(storagePath);
        Path filePath = storagePath.resolve(file.getName() + "." + file.getFileType().getType());
        Files.write(filePath, file.getData());
    }

    private boolean isRootFolder(Folder folder) {
        return "root".equals(folder.getName()) && folder.getParent() == null;
    }
}