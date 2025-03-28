package org.example.services;

import org.example.domain.model.*;
import org.example.domain.repository.FolderRepository;
import org.example.domain.repository.FileTypeRepository;
import org.example.domain.repository.FileRepository;
import org.example.domain.repository.FolderHierarchyRepository;
import org.example.dto.request.CreateFileRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.*;

@Service
public class FileService {
    private final FileRepository fileRepository; // Add this
    private final FolderRepository folderRepository;
    private final FileTypeRepository fileTypeRepository;
    private final FolderHierarchyRepository folderHierarchyRepository;

    @Autowired
    public FileService(FileRepository fileRepository, // Add this
                       FolderRepository folderRepository,
                       FileTypeRepository fileTypeRepository,
                       FolderHierarchyRepository folderHierarchyRepository) {
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
        this.fileTypeRepository = fileTypeRepository;
        this.folderHierarchyRepository = folderHierarchyRepository;
    }

    @Transactional
    public FileMetadata createFile(CreateFileRequest request) throws IOException {
        // Handle null/empty folderPath (default to "root")
        String folderPath = request.getFolderPath();
        if (folderPath == null || folderPath.trim().isEmpty()) {
            folderPath = "root"; // Default to root folder
        }

        List<String> pathSegments = Arrays.asList(folderPath.split("/"));

        // Ensure root folder exists
        Folder parentFolder = ensureFolderStructure(pathSegments);

        // Create file metadata
        FileMetadata file = new FileMetadata();
        file.setName(request.getName());
        file.setSize(request.getSize());
        file.setFolder(parentFolder);
        file.setData(Base64.getDecoder().decode(request.getData()));
        file.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        file.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        // Compute and set the full path
        String computedPath = constructFilePath(parentFolder) + "/" + request.getName();
        file.setPath(computedPath);

        // Set file type
        FileType fileType = fileTypeRepository.findByType(request.getFileType())
                .orElseThrow(() -> new IllegalArgumentException("Invalid file type"));
        file.setFileType(fileType);

        // Save to database (UNCOMMENT THIS LINE)
        FileMetadata savedFile = fileRepository.save(file); // <-- Critical fix

        // Save to filesystem
        saveToDisk(parentFolder, savedFile); // Use savedFile

        return savedFile;
    }

    private Folder ensureFolderStructure(List<String> pathSegments) {
        Folder currentParent = null;

        for (String segment : pathSegments) {
            if (segment.isEmpty()) continue;

            // Create final copy for lambda safety
            final Folder finalCurrentParent = currentParent;

            Folder folder = folderRepository.findByNameAndParent(segment, finalCurrentParent)
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

            currentParent = folder;
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
        // 1. Define root directory (create it if missing)
        Path rootDir = Paths.get("public").toAbsolutePath().normalize();
        Files.createDirectories(rootDir); // Creates "public" if it doesn't exist

        // 2. Build folder hierarchy path
        Path storagePath = rootDir;
        Folder current = parentFolder;
        while (current != null) {
            storagePath = storagePath.resolve(current.getName());
            current = current.getParent();
        }

        // 3. Create directories and save file
        Files.createDirectories(storagePath);
        Path filePath = storagePath.resolve(file.getName() + "." + file.getFileType().getType());
        Files.write(filePath, file.getData());
    }
}