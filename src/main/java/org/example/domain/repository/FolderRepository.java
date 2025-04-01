package org.example.domain.repository;

import org.example.domain.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FolderRepository extends JpaRepository<Folder, UUID> {
    Optional<Folder> findByNameAndParent(String name, Folder parent);
    boolean existsByNameAndParent(String name, Folder parent);
    List<Folder> findByParentId(UUID parentId);

    List<Folder> findByParent(Folder parent);
    @Query("SELECT fh.folder.id FROM FolderHierarchy fh WHERE fh.ancestor.id = :folderId")
    List<UUID> findDescendantIds(@Param("folderId") UUID folderId);

    @Modifying
    @Query("DELETE FROM FolderHierarchy fh WHERE fh.folder.id IN :folderIds")
    void deleteByFolderIdIn(@Param("folderIds") List<UUID> folderIds);

    @Modifying
    @Query("DELETE FROM FolderHierarchy fh WHERE fh.ancestor.id IN :ancestorIds")
    void deleteByAncestorIdIn(@Param("ancestorIds") List<UUID> ancestorIds);
}