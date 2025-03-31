package org.example.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindFilesRequest {
    private String folderPath;  // e.g., "work/x/file"
    private String fileType;    // e.g., "txt"
}