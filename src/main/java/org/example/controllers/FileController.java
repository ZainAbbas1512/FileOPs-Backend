package org.example.controllers;

import org.example.domain.model.FileMetadata;
import org.example.domain.model.FileType;
import org.example.dto.request.CreateFileRequest;
import org.example.dto.request.UpdateFileRequest;
import org.example.dto.response.FileResponse;
import org.example.dto.response.DeleteFileResponse;
import org.example.services.FileService;
import org.example.domain.repository.FileTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;
    private final FileTypeRepository fileTypeRepository;

    @Autowired
    public FileController(FileService fileService, FileTypeRepository fileTypeRepository) {
        this.fileService = fileService;
        this.fileTypeRepository = fileTypeRepository;
    }

    @PostMapping
    public ResponseEntity<FileResponse> createFile(@Validated @RequestBody CreateFileRequest request) {
        // Map the CreateFileRequest DTO to the FileMetadata entity
        FileMetadata file = new FileMetadata();
        file.setName(request.getName());
        file.setPath(request.getPath());
        file.setSize(request.getSize());
        // Decode the Base64-encoded data into a byte array
        byte[] decodedData = Base64.getDecoder().decode(request.getData());
        file.setData(decodedData);

        // Retrieve the FileType using the fileTypeId provided in the request.
//        FileType fileType = fileTypeRepository.findById(UUID.fromString(request.getFileTypeId()))
//                .orElseThrow(() -> new IllegalArgumentException("Invalid file type id"));
//        file.setFileType(fileType);

        FileMetadata createdFile = fileService.createFile(file);
        return new ResponseEntity<>(mapToResponse(createdFile), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileResponse> getFile(@PathVariable UUID id) {
        Optional<FileMetadata> fileOpt = fileService.getFileById(id);
        return fileOpt.map(file -> new ResponseEntity<>(mapToResponse(file), HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<FileResponse>> getAllFiles() {
        List<FileResponse> responses = fileService.getAllFiles()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FileResponse> updateFile(@PathVariable UUID id,
                                                   @Validated @RequestBody UpdateFileRequest request) {
        Optional<FileMetadata> optionalFile = fileService.getFileById(id);
        if (!optionalFile.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        FileMetadata existingFile = optionalFile.get();
        existingFile.setName(request.getName());
        existingFile.setPath(request.getPath());
        existingFile.setSize(request.getSize());
        // Optionally update the file data if needed.

        FileMetadata updatedFile = fileService.updateFile(existingFile);
        return new ResponseEntity<>(mapToResponse(updatedFile), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteFileResponse> deleteFile(@PathVariable UUID id) {
        fileService.deleteFile(id);
        DeleteFileResponse response = new DeleteFileResponse(id.toString(), "File deleted successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private FileResponse mapToResponse(FileMetadata file) {
        FileResponse response = new FileResponse();
        response.setId(file.getId().toString());
        response.setName(file.getName());
        response.setPath(file.getPath());
        response.setSize(file.getSize());
        response.setCreatedAt(file.getCreatedAt());
        response.setUpdatedAt(file.getUpdatedAt());
        return response;
    }
}
