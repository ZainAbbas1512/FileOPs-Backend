package org.example.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class RenameFolderRequest {
    @NotBlank(message = "New name is required")
    private String newName;

}