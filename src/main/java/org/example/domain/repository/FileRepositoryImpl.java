package org.example.domain.repository;

import org.example.domain.model.FileMetadata;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
@Transactional
public class FileRepositoryImpl {
}
