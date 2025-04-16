package com.shopsavvy.shopshavvy.service;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileStorageService {
    private final MessageSource messageSource;

    private Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }


    @Value("${file.storage.base-path}")
    private String BASE_PATH;

    private static String[] ALLOWED_FORMATS = {"jpg", "jpeg", "png", "bmp"};

    public String saveOrUpdateUserPhoto(String userId, MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new BadRequestException(messageSource.getMessage("error.invalid.file", null, getCurrentLocale()));
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BadRequestException(messageSource.getMessage("error.invalid.filename", null, getCurrentLocale()));
        }

        validateFileFormat(originalFilename);
        validateMimeType(file.getContentType());

        try {
            Path userDirectory = Paths.get(BASE_PATH, "users", userId);
            Files.createDirectories(userDirectory);

            boolean photoExists = Files.list(userDirectory).findAny().isPresent();

            deleteExistingPhoto(userDirectory);

            String fileExtension = getFileExtension(originalFilename);
            Path filePath = userDirectory.resolve(userId + "." + fileExtension);
            file.transferTo(filePath.toFile());

            return photoExists ?
                    messageSource.getMessage("success.photo.updated", null, getCurrentLocale()) :
                    messageSource.getMessage("success.photo.uploaded", null, getCurrentLocale());
        } catch (IOException e) {
            throw new IOException(messageSource.getMessage("error.file.store", null, getCurrentLocale()), e);
        }
    }

    public String deleteUserPhoto(String userId) throws IOException {
        Path userDirectory = Paths.get(BASE_PATH, "users", userId);
        if (Files.exists(userDirectory)) {
            deleteExistingPhoto(userDirectory);
        } else {
            throw new BadRequestException(messageSource.getMessage("error.photo.not.found", null, getCurrentLocale()));
        }
        return messageSource.getMessage("success.photo.deleted", null, getCurrentLocale());
    }

    private void deleteExistingPhoto(Path userDirectory) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(userDirectory)) {
            for (Path file : stream) {
                Files.deleteIfExists(file);
            }
        }
    }

    public String getUserImageUrl(String userId) {
        Path userDirectory = Path.of(BASE_PATH, "users", userId);

        if (!Files.exists(userDirectory)) {
            return null;
        }

        try {
            Optional<Path> imageFile = Files.list(userDirectory)
                    .filter(file -> file.getFileName().toString().startsWith(userId + "."))
                    .findFirst();

            if (imageFile.isPresent()) {
                String fileName = imageFile.get().getFileName().toString();
                return "/users/" + userId + "/" + fileName;
            }
        } catch (IOException e) {
            throw new RuntimeException(messageSource.getMessage("error.image.directory.access", null, getCurrentLocale()));
        }
        return null;
    }

    private void validateFileFormat(String fileName) throws BadRequestException {
        String fileExtension = getFileExtension(fileName);
        boolean isValid = Arrays.stream(ALLOWED_FORMATS)
                .anyMatch(format -> format.equalsIgnoreCase(fileExtension));

        if (!isValid) {
            throw new BadRequestException(messageSource.getMessage("error.invalid.file.format", null, getCurrentLocale()));
        }
    }

    private void validateMimeType(String contentType) throws BadRequestException {
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException(messageSource.getMessage("error.invalid.file.contenttype", null, getCurrentLocale()));
        }
    }

    private String getFileExtension(String fileName) throws BadRequestException {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new BadRequestException(messageSource.getMessage("error.invalid.filename", null, getCurrentLocale()));
        }

        fileName = fileName.trim().replaceAll("[\"']", "");

        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            throw new BadRequestException(messageSource.getMessage("error.invalid.filename", null, getCurrentLocale()));
        }

        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
}