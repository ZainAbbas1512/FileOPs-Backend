package org.example.controllers;

import org.example.domain.model.FileType;
import org.example.dto.response.FileTypeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.example.dto.request.FileTypeRequest;
import org.example.services.FileTypeService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fileType")
@CrossOrigin(origins = "http://localhost:5500")
public class FileTypeController {

    private final FileTypeService fileTypeService;

    @Autowired
    public FileTypeController(FileTypeService fileTypeService) {
        this.fileTypeService = fileTypeService;
    }

    @GetMapping
    public ResponseEntity<List<FileTypeResponse>> getFileType() {
        try {
            List<FileType> files = fileTypeService.getFileTypes();
            List<FileTypeResponse> response = files.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createFileType(@RequestBody FileTypeRequest request) {
        try {
            return new ResponseEntity<>(fileTypeService.AddFileType(request), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFileType(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(fileTypeService.getFileTypeById(id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    private FileTypeResponse mapToResponse(FileType file) {
        FileTypeResponse response = new FileTypeResponse();
        response.setId(file.getId());
        response.setType(file.getType());
        return response;
    }
}
