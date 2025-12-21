package com.albaraka_bank.modules.operation.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileSystemStorageService implements StoragePort {

    private final Path rootLocation;

    public FileSystemStorageService(@Value("${app.storage.location:uploads}") String storageLocation) {
        this.rootLocation = Paths.get(storageLocation);
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    @Override
    public String store(MultipartFile file, Long operationId) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Cannot store empty file");
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";

            String storedFilename = operationId + "_" + UUID.randomUUID() + extension;
            Path destinationFile = rootLocation.resolve(Paths.get(storedFilename)).normalize().toAbsolutePath();

            Files.copy(file.getInputStream(), destinationFile);

            return destinationFile.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Override
    public void delete(String path) {
        try {
            Files.deleteIfExists(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }
}

