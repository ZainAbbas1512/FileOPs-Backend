package org.example.domain.repository;

import org.example.domain.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface FileRepository extends JpaRepository<FileMetadata, UUID> {
}