package org.example.domain.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "file_type", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"type"})
})
public class FileType {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String type; // e.g., pdf, png, txt, etc.

    public FileType() {}

}
