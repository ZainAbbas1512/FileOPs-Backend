package org.example.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class FileTypeRequest {
    @NotBlank(message = "File type is required")
    private String fileType;

}