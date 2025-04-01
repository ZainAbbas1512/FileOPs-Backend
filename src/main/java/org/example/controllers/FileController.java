package org.example.controllers;

import org.example.domain.model.FileMetadata;
import org.example.domain.model.Folder;
import org.example.dto.request.*;
import org.example.dto.response.FileResponse;
import org.example.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "http://localhost:5500")
public class FileController {

    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @GetMapping
    public ResponseEntity<List<FileResponse>> getAllFiles() {
        try {
            List<FileMetadata> files = fileService.findAll();
            List<FileResponse> response = files.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createFile(@Valid @RequestBody CreateFileRequest request) {
        try {
            FileMetadata createdFile = fileService.createFile(request);
            return new ResponseEntity<>(mapToResponse(createdFile), HttpStatus.CREATED);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deleteFile(@Valid @RequestBody DeleteFileRequest request) {
        try {
            fileService.deleteFile(request);
            return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFileById(@PathVariable UUID id) {
        try {
            FileResponse response = fileService.getFileById(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error retrieving file");
        }
    }

    @PostMapping("/folder-files")
    public ResponseEntity<?> findFilesInFolder(@RequestBody FolderFilesRequest request) {
        try {
            List<FileResponse> files = fileService.findAllFilesInFolder(request);
            return ResponseEntity.ok(files);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error searching files");
        }
    }

    @PutMapping
    public ResponseEntity<?> updateFile(@RequestBody UpdateFileRequest request) {
        try {
            if (request.getId() == null) {
                throw new IllegalArgumentException("File ID is required");
            }
            FileResponse response = fileService.updateFile(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/by-type")
    public ResponseEntity<?> getFilesByType(
            @Valid @RequestBody FileTypeRequest request) {
        try {
            List<FileResponse> files = fileService.getAllFilesByFileType(request.getFileType());
            return ResponseEntity.ok(files);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", ex.getMessage())
            );
        }
    }

    @PostMapping("/folder-files-by-type")
    public ResponseEntity<?> findFilesInFolderByType(
            @RequestBody FolderFilesByTypeRequest request) {
        try {
            List<FileResponse> files = fileService.findAllFilesInFolderByType(request);
            return ResponseEntity.ok(files);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error searching files");
        }
    }

    private FileResponse mapToResponse(FileMetadata file) {
        FileResponse response = new FileResponse();
        response.setId(file.getId());
        response.setName(file.getName());
        response.setPath(constructFilePath(file.getFolder()));
        response.setSize(file.getSize());
        response.setCreatedAt(file.getCreatedAt());
        response.setUpdatedAt(file.getUpdatedAt());
        return response;
    }

    private String constructFilePath(Folder folder) {
        StringBuilder path = new StringBuilder();
        while (folder != null) {
            path.insert(0, "/" + folder.getName());
            folder = folder.getParent();
        }
        return path.toString().replaceFirst("/root", "");
    }
}