package org.example.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
public class FolderResponse {

    private UUID id;
    private String name;
    private String path;
    private String parentId;
    private Timestamp createdAt;
    private Timestamp updatedAt;

}