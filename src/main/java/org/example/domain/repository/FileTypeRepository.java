package org.example.domain.repository;

import org.example.domain.model.FileMetadata;
import org.example.domain.model.FileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileTypeRepository extends JpaRepository<FileType, UUID> {
    Optional<FileType> findByType(String type);
    boolean existsByType(String type);
    @Query("SELECT f FROM FileMetadata f JOIN FETCH f.fileType WHERE f.id = :id")
    Optional<FileMetadata> findByIdWithFileType(UUID id);
}