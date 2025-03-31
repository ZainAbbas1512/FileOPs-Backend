package org.example.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Setter
@Getter
public class FileResponse {
    private UUID id;
    private String name;
    private Long size;
    private String fileType;
    private String path;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Constructor
    public  FileResponse() {}
    public FileResponse(UUID id, String name, Long size, String fileType, String path, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.fileType = fileType;
        this.path = path;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}

