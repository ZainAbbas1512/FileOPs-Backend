package org.example.domain.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class FolderHierarchyId implements Serializable {

    @Column(name = "folder_id")
    private UUID folderId;

    @Column(name = "ancestor_id")
    private UUID ancestorId;

    public FolderHierarchyId() {}

    public FolderHierarchyId(UUID folderId, UUID ancestorId) {
        this.folderId = folderId;
        this.ancestorId = ancestorId;
    }

    // Getters and Setters

    public UUID getFolderId() {
        return folderId;
    }
    public void setFolderId(UUID folderId) {
        this.folderId = folderId;
    }
    public UUID getAncestorId() {
        return ancestorId;
    }
    public void setAncestorId(UUID ancestorId) {
        this.ancestorId = ancestorId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FolderHierarchyId)) return false;
        FolderHierarchyId that = (FolderHierarchyId) o;
        return Objects.equals(getFolderId(), that.getFolderId()) &&
                Objects.equals(getAncestorId(), that.getAncestorId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFolderId(), getAncestorId());
    }
}
