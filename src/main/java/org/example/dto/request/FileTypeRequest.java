package org.example.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class FileTypeRequest {
    // Getters and Setters
    @NotBlank(message = "File type is required")
    private String fileType;

}