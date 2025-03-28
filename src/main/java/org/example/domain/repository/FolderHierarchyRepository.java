package org.example.domain.repository;

import org.example.domain.model.FolderHierarchy;
import org.example.domain.model.FolderHierarchyId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface FolderHierarchyRepository extends JpaRepository<FolderHierarchy, FolderHierarchyId> {
    List<FolderHierarchy> findByFolderId(UUID folderId);
}