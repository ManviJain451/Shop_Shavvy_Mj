package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {
    private final MessageSource messageSource;

    private Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    @Value("${file.storage.base-path}")
    private String basePath;

    private static String[] allowedFormats = {"jpg", "jpeg", "png", "bmp"};
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;


    public String saveOrUpdateUserPhoto(String userId, MultipartFile file) throws IOException {
        log.info("Saving photo for user: {}", userId);

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
            Path userDirectory = Paths.get(basePath, "users", userId);
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
            log.error("Failed to store file for user {}", userId);
            throw new IOException(messageSource.getMessage("error.file.store", null, getCurrentLocale()), e);
        }
    }

    public String deleteUserPhoto(String userId) throws IOException {
        log.info("Deleting photo for user: {}", userId);
        Path userDirectory = Paths.get(basePath, "users", userId);
        if (Files.exists(userDirectory)) {
            deleteExistingPhoto(userDirectory);
        } else {
            throw new ResourceNotFoundException(messageSource.getMessage("error.photo.not.found", null, getCurrentLocale()));
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

    public String getUserImageUrl(String userId) throws IOException {
        Path userDirectory = Path.of(basePath, "users", userId);

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
            log.error("Failed to access image directory for user {}", userId);
            throw new IOException(messageSource.getMessage("error.image.directory.access", null, getCurrentLocale()));
        }
        return null;
    }

    private void validateFileFormat(String fileName) throws BadRequestException {
        String fileExtension = getFileExtension(fileName);
        boolean isValid = Arrays.stream(allowedFormats)
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

    public String saveProductVariationImage(String productId, String variationId, MultipartFile file) throws IOException {
        log.info("Saving primary image for product: {}, variation: {}", productId, variationId);
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
            Path variationDirectory = Paths.get(basePath, "products", productId, "variations", variationId);
            Files.createDirectories(variationDirectory);

            String fileExtension = getFileExtension(originalFilename);
            String fileName = variationId + "." + fileExtension;
            Path filePath = variationDirectory.resolve(fileName);

            file.transferTo(filePath.toFile());

            return fileName;
        } catch (IOException e) {
            log.error("Failed to store file for product: {}, variation: {}", productId, variationId);
            throw new IOException(
                    messageSource.getMessage("error.file.store", null, getCurrentLocale()), e);
        }
    }

    public void saveSecondaryImages(String productId, String variationId, List<MultipartFile> files) throws IOException{
        log.info("Saving secondary images for product: {}, variation: {}", productId, variationId);
        if (files == null || files.isEmpty()) {
            return;
        }

        Path variationDirectory = Paths.get(basePath, "products", productId, "variations", variationId);
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
        Path imagePath = Paths.get(basePath, "products", productId, "variations", variationId, imageName);
        if (!Files.exists(imagePath)) {
            return null;
        }
        return "/products/" + productId + "/variations/" + variationId + "/" + imageName;
    }

    public List<String> getProductVariationSecondaryImageUrls(String productId, String variationId, String primaryImageName) throws IOException {
        List<String> secondaryImageUrls = new ArrayList<>();
        Path variationDirectory = Paths.get(basePath, "products", productId, "variations", variationId);

        if (!Files.exists(variationDirectory)) {
            return secondaryImageUrls;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(variationDirectory)) {
            for (Path file : stream) {
                String fileName = file.getFileName().toString();
                if (!fileName.equals(primaryImageName)) {
                    String url = "/products/" + productId + "/variations/" + variationId + "/" + fileName;
                    secondaryImageUrls.add(url);
                }
            }
        } catch (IOException e) {
            log.error("Failed to access image directory for product: {}, variation: {}", productId, variationId);
            throw new IOException(messageSource.getMessage("error.image.directory.access", null, getCurrentLocale()));
        }

        return secondaryImageUrls;
    }

    public void deletePrimaryProductVariationImage(String productId, String variationId) throws IOException {
        Path variationDirectory = Paths.get(basePath, "products", productId, "variations", variationId);
        if (!Files.exists(variationDirectory)) {
            return ;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(variationDirectory)) {
            for (Path file : stream) {
                String fileName = file.getFileName().toString();
                if (fileName.matches(variationId + "\\.[^.]+")) {
                    Files.deleteIfExists(file);
                    log.info("Deleted primary image {} for variation {}", fileName, variationId);
                    break;
                }
            }
        } catch (IOException e) {
            log.error("Failed to delete primary image for product: {}, variation: {}", productId, variationId, e);
            throw new IOException(
                    messageSource.getMessage("error.image.delete", null, getCurrentLocale()), e);
        }

    }

    public void deleteSecondaryProductVariationImages(String productId, String variationId, int count) throws IOException {
        if (count <= 0) {
            return ;
        }
        Path variationDirectory = Paths.get(basePath, "products", productId, "variations", variationId);
        if (!Files.exists(variationDirectory)) {
            return ;
        }
        Map<Integer, Path> secondaryImages = new TreeMap<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(variationDirectory)) {
            for (Path file : stream) {
                String fileName = file.getFileName().toString();
                if (fileName.matches(variationId + "_\\d+\\.[^.]+")) {
                    Matcher matcher = Pattern.compile(variationId + "_(\\d+)\\.[^.]+").matcher(fileName);
                    if (matcher.find()) {
                        int imageNumber = Integer.parseInt(matcher.group(1));
                        secondaryImages.put(imageNumber, file);
                    }
                }
            }
        }

        int deletedCount = 0;
        for (Map.Entry<Integer, Path> entry : secondaryImages.entrySet()) {
            if (deletedCount >= count) {
                break;
            }
            try {
                Files.deleteIfExists(entry.getValue());
                deletedCount++;
                log.info("Deleted secondary image #{} for variation {}", entry.getKey(), variationId);
            } catch (IOException e) {
                log.error("Failed to delete secondary image #{} for variation {}", entry.getKey(), variationId, e);
            }
        }

        if (deletedCount < count) {
            log.warn("Requested to delete {} secondary images, but only found and deleted {}", count, deletedCount);
        }
    }


}