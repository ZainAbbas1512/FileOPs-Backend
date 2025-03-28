package org.example.domain.repository;

import org.example.domain.model.FileType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface FileTypeRepository extends JpaRepository<FileType, UUID> {
    Optional<FileType> findByType(String type);
}