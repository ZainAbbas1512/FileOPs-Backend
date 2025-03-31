package org.example.config;

import org.example.domain.model.FileType;
import org.example.domain.model.Folder;
import org.example.domain.repository.FileTypeRepository;
import org.example.domain.repository.FolderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

@Configuration
public class DataInitializer {

    private static final List<String> DEFAULT_FILE_TYPES = Arrays.asList(
            "pdf", "png", "txt", "docx", "jpg"
    );
    @Bean
    CommandLineRunner initRootFolder(FolderRepository folderRepository) {
        return args -> {
            // Create root folder if missing
            folderRepository.findByNameAndParent("root", null)
                    .orElseGet(() -> {
                        Folder root = new Folder();
                        root.setName("root");
                        root.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                        root.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
                        return folderRepository.save(root);
                    });
        };
    }

    @Bean
    CommandLineRunner initFileTypes(FileTypeRepository fileTypeRepository) {
        return args -> {
            for (String type : DEFAULT_FILE_TYPES) {
                if (fileTypeRepository.findByType(type).isEmpty()) {
                    FileType fileType = new FileType();
                    fileType.setType(type);
                    fileTypeRepository.save(fileType);
                    System.out.println("Inserted file type: " + type); // Optional logging
                }
            }
        };
    }
}