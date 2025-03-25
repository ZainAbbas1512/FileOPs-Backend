package org.example.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CreateFileRequest {

    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotBlank(message = "Path is mandatory")
    private String path;

    @NotNull(message = "Size is mandatory")
    private Long size;

    // File content as a Base64-encoded string
    @NotBlank(message = "Data is mandatory")
    private String data;

    // New: file type id (Base64 string representing the UUID)
    @NotBlank(message = "File type id is mandatory")
    private String fileTypeId;

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
    public String getData() {
        return data;
    }
    public void setData(String data) {
        this.data = data;
    }
    public String getFileTypeId() {
        return fileTypeId;
    }
    public void setFileTypeId(String fileTypeId) {
        this.fileTypeId = fileTypeId;
    }
}
