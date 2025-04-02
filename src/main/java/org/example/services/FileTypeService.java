package org.example.services;

import org.example.domain.model.FileType;
import org.example.domain.repository.FileTypeRepository;
import org.example.dto.request.FileTypeRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileTypeService {
    private final FileTypeRepository fileTypeRepository;

    public FileTypeService(FileTypeRepository fileTypeRepository) {
        this.fileTypeRepository = fileTypeRepository;
    }

    @Transactional
    public List<FileType> getFileTypes() {
        return fileTypeRepository.findAll();
    }

    @Transactional
    public FileType AddFileType(FileTypeRequest request) {
        String typeName = request.getFileType().toLowerCase().trim();

        if (fileTypeRepository.existsByType(typeName)) {
            throw new IllegalArgumentException("File type '" + typeName + "' already exists");
        }

        FileType newFileType = new FileType();
        newFileType.setType(typeName);

        return fileTypeRepository.save(newFileType);
    }

    @Transactional(readOnly = true)
    public FileType getFileTypeById(UUID id) {
        return fileTypeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("File type not found with ID: " + id));
    }
}
