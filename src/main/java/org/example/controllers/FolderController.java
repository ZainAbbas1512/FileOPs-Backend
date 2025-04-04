package org.example.controllers;

import org.example.dto.request.CreateFolderRequest;
import org.example.dto.request.RenameFolderRequest;
import org.example.dto.response.FolderResponse;
import org.example.services.FolderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/folders")
@CrossOrigin(origins = "http://localhost:3000")
public class FolderController {

    private final FolderService folderService;

    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @GetMapping
    public List<FolderResponse> getAllFolders() {
        return folderService.getAllFolders();
    }

    @PostMapping
    public ResponseEntity<?> createFolder(@Valid @RequestBody CreateFolderRequest request,
                                          BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Path is required"));
        }
        try {
            return ResponseEntity.ok(folderService.createFolder(request.getPath()));
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{folderId}/rename")
    public ResponseEntity<?> renameFolder(@PathVariable UUID folderId,
                                          @Valid @RequestBody RenameFolderRequest request) {
        try {
            return ResponseEntity.ok(folderService.renameFolder(folderId, request.getNewName()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid folder ID"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Filesystem operation failed: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<?> deleteFolder(@PathVariable UUID folderId) {
        try {
            folderService.deleteFolder(folderId);
            // Return a response with a message for frontend consistency
            return ResponseEntity.ok(Map.of("message", "Folder deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid folder ID"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Deletion failed: " + e.getMessage()));
        }
    }

    @GetMapping("/get-all-folders-of-specific-folder/{id}")
    public ResponseEntity<?> getFoldersByParent(@PathVariable UUID id) {
        try {
            List<FolderResponse> response = folderService.getFoldersByParent(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", HttpStatus.BAD_REQUEST.value(),
                    "message", e.getMessage(),
                    "timestamp", Instant.now()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "message", "Error processing request",
                    "timestamp", Instant.now()
            ));
        }
    }
}