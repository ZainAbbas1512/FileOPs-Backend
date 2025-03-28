package org.example.domain.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import org.hibernate.annotations.Parameter;
import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "file", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"folder_id", "name"})
})
public class FileMetadata {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator",
            parameters = {
                    @Parameter(
                            name = "uuid_gen_strategy_class",
                            value = "org.hibernate.id.uuid.CustomVersionOneStrategy"
                    )
            }
    )
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Setter
    @Column(nullable = false)
    private String name;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", nullable = false)
    private Folder folder;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_type_id", nullable = false)
    private FileType fileType;

    @Setter
    @Column(nullable = false)
    private Long size;

    @Setter
    @Lob
    @Column(name = "data", nullable = false)
    private byte[] data;

    @Setter
    @Column(nullable = false)
    private String path;

    @Setter
    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT NOW()", nullable = false)
    private Timestamp createdAt;

    @Setter
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT NOW()", nullable = false)
    private Timestamp updatedAt;

    public FileMetadata() {}
}