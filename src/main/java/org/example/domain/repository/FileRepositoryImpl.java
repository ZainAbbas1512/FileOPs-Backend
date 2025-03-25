package org.example.domain.repository;

import org.example.domain.model.FileMetadata;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
@Transactional
public class FileRepositoryImpl implements FileRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<FileMetadata> findFilesLargerThan(Long size) {
        return entityManager
                .createQuery("SELECT f FROM FileMetadata f WHERE f.size > :size", FileMetadata.class)
                .setParameter("size", size)
                .getResultList();
    }
}
