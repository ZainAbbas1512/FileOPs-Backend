package org.example.config;

import org.example.domain.model.FileType;
import org.example.domain.repository.FileTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Arrays;
import java.util.List;

@Configuration
public class DataInitializer {

    // List of default file types to insert
    private static final List<String> DEFAULT_FILE_TYPES = Arrays.asList(
            "pdf", "png", "txt", "docx", "jpg"
    );

    @Bean
    CommandLineRunner initFileTypes(FileTypeRepository fileTypeRepository) {
        return args -> {
            for (String type : DEFAULT_FILE_TYPES) {
                // Check if the file type already exists
                if (!fileTypeRepository.findByType(type).isPresent()) {
                    FileType fileType = new FileType();
                    fileType.setType(type);
                    fileTypeRepository.save(fileType);
                    System.out.println("Inserted file type: " + type); // Optional logging
                }
            }
        };
    }
}