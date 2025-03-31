package org.example.dto.request;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class DeleteFileRequest {
    @NotBlank(message = "Full path is mandatory")
    private String fullPath; // e.g., "wor/x/y/x/y/file2"

    @NotBlank(message = "File type is mandatory")
    private String fileType;
}