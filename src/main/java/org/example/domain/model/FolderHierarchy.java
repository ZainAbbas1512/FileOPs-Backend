package org.example.domain.model;

import javax.persistence.*;

@Entity
@Table(name = "folder_hierarchy")
public class FolderHierarchy {

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

    // Getters and Setters
    public FolderHierarchyId getId() {
        return id;
    }
    public void setId(FolderHierarchyId id) {
        this.id = id;
    }
    public Folder getFolder() {
        return folder;
    }
    public void setFolder(Folder folder) {
        this.folder = folder;
    }
    public Folder getAncestor() {
        return ancestor;
    }
    public void setAncestor(Folder ancestor) {
        this.ancestor = ancestor;
    }
    public int getDepth() {
        return depth;
    }
    public void setDepth(int depth) {
        this.depth = depth;
    }
}
