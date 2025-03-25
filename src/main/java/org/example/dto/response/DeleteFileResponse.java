package org.example.dto.response;

public class DeleteFileResponse {

    private String fileId;
    private String message;

    public DeleteFileResponse() {
    }

    public DeleteFileResponse(String fileId, String message) {
        this.fileId = fileId;
        this.message = message;
    }

    // Getters and Setters
    public String getFileId() {
        return fileId;
    }
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
