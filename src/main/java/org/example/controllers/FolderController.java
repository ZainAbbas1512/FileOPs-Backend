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
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/folders")
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
    public ResponseEntity<?> renameFolder(@PathVariable String folderId,
                                          @Valid @RequestBody RenameFolderRequest request) {
        try {
            UUID uuid = UUID.fromString(folderId);
            return ResponseEntity.ok(folderService.renameFolder(uuid, request.getNewName()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid folder ID"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Filesystem operation failed: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<?> deleteFolder(@PathVariable String folderId) {
        try {
            UUID uuid = UUID.fromString(folderId);
            folderService.deleteFolder(uuid);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid folder ID"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Deletion failed: " + e.getMessage()));
        }
    }

    @GetMapping("/by-parent")
    public List<FolderResponse> getByParent(@RequestParam(required = false) String parentId) {
        try {
            UUID parentUuid = parentId != null ? UUID.fromString(parentId) : null;
            return folderService.getFoldersByParent(parentUuid);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parent ID");
        }
    }
}