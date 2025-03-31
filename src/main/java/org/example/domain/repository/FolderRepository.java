package org.example.domain.repository;

import org.example.domain.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FolderRepository extends JpaRepository<Folder, UUID> {
    Optional<Folder> findByNameAndParent(String name, Folder parent);
    boolean existsByNameAndParent(String name, Folder parent);
    List<Folder> findByParentId(UUID parentId);

    List<Folder> findByParent(Folder parent);
}