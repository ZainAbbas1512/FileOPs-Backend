package org.example.domain.repository;

import org.example.domain.model.FolderHierarchy;
import org.example.domain.model.FolderHierarchyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FolderHierarchyRepository extends JpaRepository<FolderHierarchy, FolderHierarchyId> {
    List<FolderHierarchy> findByFolderId(UUID folderId);
    @Query("SELECT fh FROM FolderHierarchy fh WHERE fh.ancestor.id = :ancestorId")
    List<FolderHierarchy> findDescendants(@Param("ancestorId") UUID ancestorId);

    @Query("SELECT fh.folder.id FROM FolderHierarchy fh WHERE fh.ancestor.id = :ancestorId")
    List<UUID> findDescendantIds(@Param("ancestorId") UUID ancestorId);

    List<FolderHierarchy> findByAncestorId(UUID id);

    void deleteByFolderIdIn(List<UUID> allFolderIds);

    void deleteByAncestorIdIn(List<UUID> allFolderIds);
}