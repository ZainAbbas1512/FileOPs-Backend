package org.example.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Setter
@Getter
public class CreateFileRequest {
    // Getters and Setters
    @NotBlank(message = "Name is mandatory")
    private String name;

    private String folderPath;

    @NotNull(message = "Size is mandatory")
    private Long size;

    @NotBlank(message = "Data is mandatory")
    private String data;

    @NotBlank(message = "File type is mandatory")
    private String fileType;

}