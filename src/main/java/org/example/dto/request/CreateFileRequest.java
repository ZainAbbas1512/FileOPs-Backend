package org.example.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CreateFileRequest {
    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotBlank(message = "Folder path is mandatory")
    private String folderPath;

    @NotNull(message = "Size is mandatory")
    private Long size;

    @NotBlank(message = "Data is mandatory")
    private String data;

    @NotBlank(message = "File type is mandatory")
    private String fileType;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getFolderPath() { return folderPath; }
    public void setFolderPath(String folderPath) { this.folderPath = folderPath; }
    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
}