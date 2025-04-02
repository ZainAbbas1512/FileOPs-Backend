package org.example.services;

import org.example.domain.model.*;
import org.example.domain.repository.FolderRepository;
import org.example.domain.repository.FileTypeRepository;
import org.example.domain.repository.FileRepository;
import org.example.domain.repository.FolderHierarchyRepository;

import org.example.dto.request.*;

import org.example.dto.response.*;

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

        // Verify existing folder structure
        Folder parentFolder;
        if (pathSegments.isEmpty()) {
            parentFolder = getRootFolder();
        } else {
            Folder current = getRootFolder();
            for (String segment : pathSegments) {
                current = folderRepository.findByNameAndParent(segment, current)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Folder path '" + segment + "' does not exist. Create the folder first."
                        ));
            }
            parentFolder = current;
        }

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
        FileMetadata file = fileRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        fileRepository.delete(file);

        // Delete from filesystem
        deleteFromDisk(file);
    }

    @Transactional(readOnly = true)
    public FileResponse getFileById(UUID id) {
        FileMetadata file = fileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("File not found with ID: " + id));

        return mapToFileResponse(file);
    }

    @Transactional(readOnly = true)
    public List<FileResponse> findAllFilesInFolder(FolderFilesRequest request) {
        // Validate input
        if (request.getFolderPath() == null || request.getFolderPath().isEmpty()) {
            throw new IllegalArgumentException("Folder path must be provided");
        }

        // Normalize the path (remove leading/trailing slashes)
        String normalizedPath = request.getFolderPath()
                .replaceAll("^/+|/+$", "");

        System.out.println(normalizedPath);
        // For root folder
        if (normalizedPath.isEmpty()) {
            Folder root = getRootFolder();
            return fileRepository.findByFolder(root).stream()
                    .map(this::mapToFileResponse)
                    .collect(Collectors.toList());
        }

        // Find files directly in the specified folder (not subfolders)
        List<FileMetadata> files = fileRepository.findAllByFolderPath(normalizedPath);

        return files.stream()
                .map(this::mapToFileResponse)
                .collect(Collectors.toList());
    }

    private Folder getRootFolder() {
        return folderRepository.findByNameAndParent("root", null)
                .orElseThrow(() -> new IllegalStateException("Root folder not found"));
    }

    @Transactional
    public FileResponse updateFile(UpdateFileRequest request) throws IOException {
        FileMetadata file = fileRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        // Capture original state before any changes
        Folder originalFolder = file.getFolder();
        String originalName = file.getName();
        FileType originalFileType = file.getFileType();
//        byte[] originalData = file.getData().clone();

        // Track changes
        boolean nameChanged = false;
        boolean folderChanged = false;
        boolean typeChanged = false;

        // Apply updates from the request
        if (request.getName() != null && !request.getName().equals(originalName)) {
            file.setName(request.getName());
            nameChanged = true;
        }
        if (request.getSize() != null) {
            file.setSize(request.getSize());
        }

        Folder newFolder;
        if (request.getFolderPath() != null) {
            List<String> pathSegments = Arrays.asList(
                    request.getFolderPath().replaceAll("^/+|/+$", "").split("/")
            );
            newFolder = ensureFolderStructure(pathSegments);
            if (!newFolder.equals(originalFolder)) {
                folderChanged = true;
                file.setFolder(newFolder);
            }
        }

        FileType newFileType;
        if (request.getFileType() != null) {
            newFileType = fileTypeRepository.findByType(request.getFileType())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid file type"));
            if (!newFileType.equals(originalFileType)) {
                typeChanged = true;
                file.setFileType(newFileType);
            }
        }

        // Check for conflicts in new location (only if relevant fields changed)
        if (folderChanged || nameChanged || typeChanged) {
            boolean conflictExists = fileRepository.existsByNameAndFolderAndFileTypeAndIdNot(
                    file.getName(),
                    file.getFolder(),
                    file.getFileType(),
                    file.getId()
            );
            if (conflictExists) {
                throw new IllegalArgumentException(
                        "A file with the same name and type already exists in the target folder"
                );
            }
        }

        // Update file path based on current state
        String newPath = constructFilePath(file.getFolder()) + "/" + file.getName();
        file.setPath(newPath);
        file.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        // Handle data changes
        boolean dataChanged = false;
        if (request.getData() != null) {
            file.setData(Base64.getDecoder().decode(request.getData()));
            dataChanged = true;
        }

        FileMetadata savedFile = fileRepository.save(file);

        // Filesystem operations
        if (folderChanged || nameChanged || typeChanged || dataChanged) {
            Path oldDir = buildDirectoryPath(originalFolder);
            Path newDir = buildDirectoryPath(savedFile.getFolder());

            String oldFileName = originalName + "." + originalFileType.getType();
            String newFileName = savedFile.getName() + "." + savedFile.getFileType().getType();

            // Delete old file if location changed
            if (folderChanged || nameChanged || typeChanged) {
                Path oldFilePath = oldDir.resolve(oldFileName);
                Files.deleteIfExists(oldFilePath);
            }

            // Write new file
            Files.createDirectories(newDir);
            Path newFilePath = newDir.resolve(newFileName);
            Files.write(newFilePath, savedFile.getData());
        }

        return mapToFileResponse(savedFile);
    }

    @Transactional(readOnly = true)
    public List<FileResponse> getAllFilesByFileType(String fileType) {
        // Validate input
        if (fileType == null || fileType.isEmpty()) {
            throw new IllegalArgumentException("File type must be provided");
        }

        // Verify file type exists
        FileType type = fileTypeRepository.findByType(fileType)
                .orElseThrow(() -> new IllegalArgumentException("Invalid file type: " + fileType));

        // Find all files with this type
        List<FileMetadata> files = fileRepository.findByFileType(type);

        return files.stream()
                .map(this::mapToFileResponse)
                .collect(Collectors.toList());
    }

    private Path buildDirectoryPath(Folder folder) {
        Path dirPath = Paths.get("public").toAbsolutePath().normalize();

        List<String> folderNames = new ArrayList<>();
        Folder current = folder;
        while (current != null && !isRootFolder(current)) {
            folderNames.add(current.getName());
            current = current.getParent();
        }
        Collections.reverse(folderNames);

        for (String name : folderNames) {
            dirPath = dirPath.resolve(name);
        }

        return dirPath;
    }

    @Transactional(readOnly = true)
    public List<FileResponse> findAllFilesInFolderByType(FolderFilesByTypeRequest request) {
        // Validate input
        if (request.getFolderPath() == null || request.getFolderPath().isEmpty()) {
            throw new IllegalArgumentException("Folder path must be provided");
        }
        if (request.getFileType() == null || request.getFileType().isEmpty()) {
            throw new IllegalArgumentException("File type must be provided");
        }

        // Normalize the path
        String normalizedPath = request.getFolderPath().replaceAll("^/+|/+$", "");

        // For root folder
        if (normalizedPath.isEmpty()) {
            Folder root = getRootFolder();
            return fileRepository.findByFolderAndFileTypeType(root, request.getFileType())
                    .stream()
                    .map(this::mapToFileResponse)
                    .collect(Collectors.toList());
        }

        // Find files in the specified folder with matching type
        List<FileMetadata> files = fileRepository.findAllByFolderPathAndFileType(
                normalizedPath,
                request.getFileType()
        );

        return files.stream()
                .map(this::mapToFileResponse)
                .collect(Collectors.toList());
    }

    private FileResponse mapToFileResponse(FileMetadata file) {
        FileResponse response = new FileResponse();
        response.setId(file.getId());
        response.setName(file.getName());
        response.setPath(file.getPath());
        response.setSize(file.getSize());
        response.setCreatedAt(file.getCreatedAt());
        response.setUpdatedAt(file.getUpdatedAt());
        response.setFileType(file.getFileType().getType());
        return response;
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