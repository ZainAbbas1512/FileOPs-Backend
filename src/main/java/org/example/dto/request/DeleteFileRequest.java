package org.example.dto.request;


import javax.validation.constraints.NotBlank;

public class DeleteFileRequest {
    @NotBlank(message = "File ID is mandatory")
    private String fileId;

    // Getters and Setters
}
