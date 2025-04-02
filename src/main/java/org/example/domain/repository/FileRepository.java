package org.example.domain.repository;

import org.example.domain.model.FileMetadata;
import org.example.domain.model.FileType;
import org.example.domain.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileRepository extends JpaRepository<FileMetadata, UUID> {
    boolean existsByNameAndFolderAndFileType(String name, Folder folder, FileType fileType);
    Optional<FileMetadata> findByPathAndFileType(String path, FileType fileType);

    List<FileMetadata> findAll();

    List<FileMetadata> findByFolder(Folder folder);
    @Query("SELECT f FROM FileMetadata f WHERE " +
            "f.path LIKE CONCAT(:folderPath, '%') AND " +
            "f.fileType.type = :fileType")
    List<FileMetadata> findFileByFolderPathAndFileNameAndFileType(
            @Param("folderPath") String folderPath,
            @Param("fileType") String fileType
    );

    @Query("SELECT f FROM FileMetadata f WHERE " +
            "f.path LIKE CONCAT(:folderPath, '/%') AND " +
            "f.path NOT LIKE CONCAT(:folderPath, '/%/%')")
    List<FileMetadata> findAllByFolderPath(@Param("folderPath") String folderPath);

    @Query("SELECT f FROM FileMetadata f WHERE " +
            "f.path LIKE CONCAT(:folderPath, '/%') AND " +
            "f.path NOT LIKE CONCAT(:folderPath, '/%/%') AND " +
            "f.fileType.type = :fileType")
    List<FileMetadata> findAllByFolderPathAndFileType(
            @Param("folderPath") String folderPath,
            @Param("fileType") String fileType
    );

    List<FileMetadata> findByFolderAndFileTypeType(Folder folder, String fileType);

    boolean existsByNameAndFolderAndFileTypeAndIdNot(String name, Folder folder, FileType fileType, UUID id);
    List<FileMetadata> findByFileType(FileType fileType);
    void deleteAllByFolderIdIn(List<UUID> folderIds);
}