package org.example.services;

import org.example.domain.model.FileMetadata;
import org.example.domain.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileService {

    private final FileRepository fileRepository;

    @Autowired
    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Transactional
    public FileMetadata createFile(FileMetadata file) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        file.setCreatedAt(now);
        file.setUpdatedAt(now);
        return fileRepository.save(file);
    }

    @Transactional
    public FileMetadata updateFile(FileMetadata file) {
        file.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        return fileRepository.save(file);
    }

    @Transactional(readOnly = true)
    public Optional<FileMetadata> getFileById(UUID id) {
        return fileRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<FileMetadata> getAllFiles() {
        return fileRepository.findAll();
    }

    @Transactional
    public void deleteFile(UUID id) {
        fileRepository.deleteById(id);
    }
}
