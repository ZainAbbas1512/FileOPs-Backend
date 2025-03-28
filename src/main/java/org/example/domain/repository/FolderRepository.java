package org.example.domain.repository;

import org.example.domain.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface FolderRepository extends JpaRepository<Folder, UUID> {
    Optional<Folder> findByNameAndParent(String name, Folder parent);
}