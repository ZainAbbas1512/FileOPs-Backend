package org.example.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class CreateFolderRequest {
    @NotBlank(message = "Path is required")
    private String path;
}