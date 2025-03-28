package org.example.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@Entity
@Table(name = "folder_hierarchy")
public class FolderHierarchy {

    // Getters and Setters
    @EmbeddedId
    private FolderHierarchyId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("folderId")
    @JoinColumn(name = "folder_id")
    private Folder folder;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ancestorId")
    @JoinColumn(name = "ancestor_id")
    private Folder ancestor;

    @Column(nullable = false)
    private int depth;

    // Constructors
    public FolderHierarchy() {}

    public FolderHierarchy(Folder folder, Folder ancestor, int depth) {
        this.folder = folder;
        this.ancestor = ancestor;
        this.depth = depth;
        this.id = new FolderHierarchyId(folder.getId(), ancestor.getId());
    }

}
