package org.example.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Setter
@Getter
public class FileResponse {

    private String id;
    private String name;
    private String path;
    private Long size;
    private Timestamp createdAt;
    private Timestamp updatedAt;

}
