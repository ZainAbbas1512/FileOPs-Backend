package org.example.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class UpdateFileRequest {
    private UUID id;            // Required
    private String name;
    private String folderPath;
    private Long size;
    private String data;        // Base64 encoded
    private String fileType;
}