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
import java.util.*;

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
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;


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

    public String saveProductVariationImage(String productId, String variationId, MultipartFile file) throws IOException, BadRequestException {
        System.out.println("primary image ");
        if (file == null || file.isEmpty()) {
            throw new BadRequestException(
                    messageSource.getMessage("error.invalid.file", null, getCurrentLocale()));
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BadRequestException(
                    messageSource.getMessage("error.invalid.filename", null, getCurrentLocale()));
        }

        validateFileFormat(originalFilename);
        validateMimeType(file.getContentType());

        try {
            System.out.println(productId +  " " + variationId);
            Path variationDirectory = Paths.get(BASE_PATH, "products", productId, "variations", variationId);
            Files.createDirectories(variationDirectory);

            String fileExtension = getFileExtension(originalFilename);
            String fileName = variationId + "." + fileExtension;
            Path filePath = variationDirectory.resolve(fileName);

            file.transferTo(filePath.toFile());

            return fileName;
        } catch (IOException e) {
            throw new IOException(
                    messageSource.getMessage("error.file.store", null, getCurrentLocale()), e);
        }
    }

    public void saveSecondaryImages(String productId, String variationId, List<MultipartFile> files) throws IOException, BadRequestException {
        if (files == null || files.isEmpty()) {
            return;
        }

        Path variationDirectory = Paths.get(BASE_PATH, "products", productId, "variations", variationId);
        Files.createDirectories(variationDirectory);

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            if (file.isEmpty()) {
                continue;
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                continue;
            }

            validateFileFormat(originalFilename);
            validateMimeType(file.getContentType());

            String fileExtension = getFileExtension(originalFilename);
            String fileName = variationId + "_" + (i + 1) + "." + fileExtension;
            Path filePath = variationDirectory.resolve(fileName);

            file.transferTo(filePath.toFile());
        }
    }

    public String getProductVariationImageUrl(String productId, String variationId, String imageName) {
        Path imagePath = Paths.get(BASE_PATH, "products", productId, "variations", variationId, imageName);
        if (!Files.exists(imagePath)) {
            return null;
        }
        return "/products/" + productId + "/variations/" + variationId + "/" + imageName;
    }

    public void deleteProductVariationImages(String productId, String variationId) throws IOException {
        Path variationDirectory = Paths.get(BASE_PATH, "products", productId, "variations", variationId);
        if (Files.exists(variationDirectory)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(variationDirectory)) {
                for (Path file : stream) {
                    Files.deleteIfExists(file);
                }
            }
            Files.deleteIfExists(variationDirectory);
        }
    }
}

