package org.example.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class UpdateFileRequest {

    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotBlank(message = "Path is mandatory")
    private String path;

    @NotNull(message = "Size is mandatory")
    private Long size;

    // Optionally add a field for updating file content if needed.
    // For now, we assume content is not updated.

    // Getters and Setters
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public Long getSize() {
        return size;
    }
    public void setSize(Long size) {
        this.size = size;
    }
}
