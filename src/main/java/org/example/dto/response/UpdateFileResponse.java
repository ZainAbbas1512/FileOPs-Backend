package org.example.dto.response;


public class UpdateFileResponse {
    private String fileId;
    private String message;

    public UpdateFileResponse(String fileId, String message) {
        this.fileId = fileId;
        this.message = message;
    }

    // Getters
}
