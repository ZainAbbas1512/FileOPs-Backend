package org.example.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FolderFilesRequest {
    private String folderPath;  // e.g., "work/x/y"
}