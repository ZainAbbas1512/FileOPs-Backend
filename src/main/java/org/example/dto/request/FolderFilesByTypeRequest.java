package org.example.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FolderFilesByTypeRequest {
    private String folderPath;  // e.g., "work/x/y"
    private String fileType;    // e.g., "pdf"
}