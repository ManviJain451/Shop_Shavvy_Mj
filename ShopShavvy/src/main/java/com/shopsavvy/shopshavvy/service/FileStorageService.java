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
        try {
            validateFileFormat(file.getOriginalFilename());

            Path userDirectory = Paths.get(BASE_PATH, "users", userId);
            Files.createDirectories(userDirectory);

            boolean photoExists = Files.list(userDirectory).findAny().isPresent();

            deleteExistingPhoto(userDirectory);

            Path filePath = userDirectory.resolve(userId + "." + getFileExtension(file.getOriginalFilename()));
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
        boolean isValid = false;
        for (String format : ALLOWED_FORMATS) {
            if (format.equalsIgnoreCase(fileExtension)) {
                isValid = true;
                break;
            }
        }
        if (!isValid) {
            throw new BadRequestException(messageSource.getMessage("error.invalid.file.format", null, getCurrentLocale()));
        }
    }

    private String getFileExtension(String fileName) throws BadRequestException {
        if (fileName == null || !fileName.contains(".")) {
            throw new BadRequestException(messageSource.getMessage("error.invalid.filename", null, getCurrentLocale()));
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
}