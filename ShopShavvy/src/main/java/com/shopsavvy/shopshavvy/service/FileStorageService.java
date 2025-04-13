package com.shopsavvy.shopshavvy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Service
public class FileStorageService {

    @Value("${file.storage.base-path}")
    private String BASE_PATH;

    private static final String[] ALLOWED_FORMATS = {"jpg", "jpeg", "png", "bmp"};

    public String saveOrUpdateUserPhoto(String userId, MultipartFile file) throws IOException {
        validateFileFormat(file.getOriginalFilename());

        Path userDirectory = Paths.get(BASE_PATH, "users", userId);
        Files.createDirectories(userDirectory);

        boolean photoExists = Files.list(userDirectory).findAny().isPresent();

        deleteExistingPhoto(userDirectory);

        Path filePath = userDirectory.resolve(userId + "." + getFileExtension(file.getOriginalFilename()));
        file.transferTo(filePath.toFile());

        return photoExists ? "Profile photo updated successfully." : "Profile photo uploaded successfully.";

    }

    public void deleteUserPhoto(String userId) throws IOException {
        Path userDirectory = Paths.get(BASE_PATH, "users", userId);
        if (Files.exists(userDirectory)) {
            deleteExistingPhoto(userDirectory);
        } else {
            throw new IllegalArgumentException("No profile photo found for the user.");
        }
    }

    private void deleteExistingPhoto(Path userDirectory) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(userDirectory)) {
            for (Path file : stream) {
                Files.deleteIfExists(file);
            }
        }
    }

    private void validateFileFormat(String fileName) {
        String fileExtension = getFileExtension(fileName);
        boolean isValid = false;
        for (String format : ALLOWED_FORMATS) {
            if (format.equalsIgnoreCase(fileExtension)) {
                isValid = true;
                break;
            }
        }
        if (!isValid) {
            throw new IllegalArgumentException("Invalid file format. Allowed formats: jpg, jpeg, png, bmp.");
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new IllegalArgumentException("Invalid file name.");
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
}