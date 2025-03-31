package org.example.domain.repository;

import org.example.domain.model.FileMetadata;
import org.example.domain.model.FileType;
import org.example.domain.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FileRepository extends JpaRepository<FileMetadata, UUID> {
    boolean existsByNameAndFolderAndFileType(String name, Folder folder, FileType fileType);
    Optional<FileMetadata> findByPathAndFileType(String path, FileType fileType);
}