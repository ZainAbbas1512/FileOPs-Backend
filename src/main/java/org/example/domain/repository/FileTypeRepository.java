package org.example.domain.repository;

import org.example.domain.model.FileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FileTypeRepository extends JpaRepository<FileType, UUID> {
}
