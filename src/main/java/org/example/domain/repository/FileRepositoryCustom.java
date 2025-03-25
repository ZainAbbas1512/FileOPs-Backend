package org.example.domain.repository;

import org.example.domain.model.FileMetadata;
import java.util.List;

public interface FileRepositoryCustom {
    List<FileMetadata> findFilesLargerThan(Long size);
}
