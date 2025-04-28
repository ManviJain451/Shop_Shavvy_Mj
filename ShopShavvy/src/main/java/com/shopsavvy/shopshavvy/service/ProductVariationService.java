package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.configuration.UserDetailsImpl;
import com.shopsavvy.shopshavvy.dto.product_dto.ProductResponseDTO;
import com.shopsavvy.shopshavvy.dto.product_dto.ProductVariationDTO;
import com.shopsavvy.shopshavvy.dto.product_dto.ProductVariationResponseDTO;
import com.shopsavvy.shopshavvy.dto.product_dto.ProductVariationUpdateDTO;
import com.shopsavvy.shopshavvy.exception.DuplicateEntryExistsException;
import com.shopsavvy.shopshavvy.exception.ResourceNotFoundException;
import com.shopsavvy.shopshavvy.exception.UserNotFoundException;
import com.shopsavvy.shopshavvy.model.categories.Category;
import com.shopsavvy.shopshavvy.model.categories.CategoryMetadataFieldValues;
import com.shopsavvy.shopshavvy.model.products.Product;
import com.shopsavvy.shopshavvy.model.products.ProductVariation;
import com.shopsavvy.shopshavvy.repository.*;
import com.shopsavvy.shopshavvy.specification.ProductVariationSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jmx.export.metadata.InvalidMetadataException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductVariationService {

    private final MessageSource messageSource;
    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;
    private final ProductVariationRepository productVariationRepository;
    private final CategoryMetadataFieldValuesRepository categoryMetadataFieldValuesRepository;
    private Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    public String addProductVariations(UserDetailsImpl userDetails, ProductVariationDTO dto, MultipartFile primaryImage, List<MultipartFile> secondaryImages) throws IOException , ResourceNotFoundException{
        log.info("Adding product variation for product ID: {}", dto.getProductId());
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("product.not.found", new Object[]{dto.getProductId()}, getCurrentLocale())));

        if (!product.isActive()) {
            throw new BadRequestException(messageSource.getMessage("product.inactive", null, getCurrentLocale()));
        }
        if(!userDetails.getUsername().equals(product.getSeller().getEmail())){
            throw new BadRequestException(
                    messageSource.getMessage("error.product.not.authorized", null, getCurrentLocale()));
        }
        Category category = product.getCategory();

        boolean alreadyExists = product.getProductVariations().stream()
                .anyMatch(v -> Objects.equals(v.getMetadata(), dto.getMetadata()));
        if (alreadyExists) {
            throw new DuplicateEntryExistsException(messageSource.getMessage("product.variation.duplicate", null, getCurrentLocale()));
        }

        validateMetadata(dto.getMetadata(), category, product);

        ProductVariation variation = new ProductVariation();
        variation.setProduct(product);
        variation.setMetadata(dto.getMetadata());
        variation.setQuantity(dto.getQuantity());
        variation.setPrice(dto.getPrice());
        variation.setActive(true);

        ProductVariation savedVariation = productVariationRepository.save(variation);

        try {
            String primaryImageKey = fileStorageService.saveProductVariationImage(product.getId(), savedVariation.getId(), primaryImage);
            savedVariation.setPrimaryImage(primaryImageKey);

            if (secondaryImages != null && !secondaryImages.isEmpty()) {
                fileStorageService.saveSecondaryImages(product.getId(), savedVariation.getId(), secondaryImages);
            }
            productVariationRepository.save(savedVariation);

        } catch (IOException e) {
            log.error("Failed to upload images for variation: {}", e.getMessage());
            throw new IOException(messageSource.getMessage("error.failed.upload.images", null, getCurrentLocale()));
        }

        return messageSource.getMessage("success.created.product.variation", null, getCurrentLocale());

    }

    private void validateMetadata(Map<String, Object> metadata, Category category, Product product) throws BadRequestException {
        if (metadata == null || metadata.isEmpty()) {
            throw new BadRequestException(messageSource.getMessage("metadata.min.one", null, getCurrentLocale()));
        }

        Set<String> newMetadataFields = metadata.keySet().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        if (!product.getProductVariations().isEmpty()) {
            Optional<ProductVariation> existingVariation = product.getProductVariations().stream()
                    .filter(v -> v.getMetadata() != null && !v.getMetadata().isEmpty())
                    .findFirst();

            if (existingVariation.isPresent()) {
                Set<String> existingFields = existingVariation.get().getMetadata().keySet().stream()
                        .map(String::toLowerCase)
                        .collect(Collectors.toSet());

                if (!existingFields.equals(newMetadataFields)) {
                    throw new InvalidMetadataException(messageSource.getMessage("metadata.structure.mismatch", null, getCurrentLocale()));
                }
            }
        }

        Map<String, Set<String>> existingMetadata = new HashMap<>();
        Category current = category;

        while (current != null) {
            List<CategoryMetadataFieldValues> metadataValues = categoryMetadataFieldValuesRepository.findByCategory(current);
            for (CategoryMetadataFieldValues value : metadataValues) {
                String fieldName = value.getCategoryMetadataField().getName().toLowerCase();
                Set<String> allowedValues = Arrays.stream(value.getValues().split(","))
                        .map(v -> v.trim().toLowerCase())
                        .collect(Collectors.toSet());
                existingMetadata.put(fieldName, allowedValues);
            }
            current = current.getParentCategory();
        }
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            String fieldName = entry.getKey().toLowerCase();
            String value = entry.getValue().toString().toLowerCase();

            if (!existingMetadata.containsKey(fieldName)) {
                throw new InvalidMetadataException(messageSource.getMessage("metadata.field.invalid", new Object[]{fieldName}, getCurrentLocale()));
            }
            if (!existingMetadata.get(fieldName).contains(value)) {
                throw new InvalidMetadataException(messageSource.getMessage("metadata.value.invalid", new Object[]{value, fieldName}, getCurrentLocale()));
            }
        }
    }

    public ProductVariationResponseDTO viewProductVariation(UserDetailsImpl userDetails, String productVariationId) throws IOException, ResourceNotFoundException {
        log.info("Fetching product variation: {}", productVariationId);
        ProductVariation variation = productVariationRepository.findById(productVariationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageSource.getMessage("error.variation.not.found", null, getCurrentLocale())));


        if (!variation.getProduct().getSeller().getEmail().equals(userDetails.getUsername())) {
            throw new BadRequestException(
                    messageSource.getMessage("error.variation.not.authorized", null, getCurrentLocale()));
        }

        Product product = variation.getProduct();
        ProductResponseDTO parentProduct = ProductResponseDTO.builder()
                .productId(product.getId())
                .productName(product.getName())
                .brand(product.getBrand())
                .description(product.getDescription())
                .cancellable(product.isCancellable())
                .returnable(product.isReturnable())
                .build();

        String primaryImageUrl = fileStorageService.getProductVariationImageUrl(product.getId(), variation.getId(), variation.getPrimaryImage());
        List<String> secondaryImagesUrl = fileStorageService.getProductVariationSecondaryImageUrls(
                product.getId(), variation.getId(), variation.getPrimaryImage());


        return ProductVariationResponseDTO.builder()
                .price(variation.getPrice())
                .quantity(variation.getQuantity())
                .metadata(variation.getMetadata())
                .primaryImage(primaryImageUrl)
                .secondaryImages(secondaryImagesUrl)
                .parentProduct(parentProduct)
                .build();

    }

    public List<ProductVariationResponseDTO> viewAllProductVariation(UserDetailsImpl userDetails,
                                                                     String productId, String sort, String order, int max,
                                                                     int offset, Map<String, Object> filter) throws BadRequestException, ResourceNotFoundException {
        log.info("Fetching all variations for product: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageSource.getMessage("error.product.not.found", null, getCurrentLocale())));

        if (!product.getSeller().getEmail().equals(userDetails.getUsername())) {
            throw new BadRequestException(
                    messageSource.getMessage("error.product.not.authorized", null, getCurrentLocale()));
        }

        Pageable pageable = PageRequest.of(offset / max, max,
                Sort.by(Sort.Direction.fromString(order.toUpperCase()), sort));

        Specification<ProductVariation> specification = ProductVariationSpecification.getByProductIdAndFilters(productId, filter);
        Page<ProductVariation> productsPage = productVariationRepository.findAll(specification, pageable);

        return productsPage.getContent().stream()
                .map(variation -> {
                    try {
                        return ProductVariationResponseDTO.builder()
                                .productVariationId(variation.getId())
                                .price(variation.getPrice())
                                .quantity(variation.getQuantity())
                                .metadata(variation.getMetadata())
                                .primaryImage(fileStorageService
                                        .getProductVariationImageUrl(productId, variation.getId(), variation.getPrimaryImage()))
                                .secondaryImages(fileStorageService.getProductVariationSecondaryImageUrls(
                                        product.getId(), variation.getId(), variation.getPrimaryImage()))
                                .build();
                    } catch (IOException e) {
                        log.error("Error retrieving images: {}", e.getMessage());
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }

    public String updateProductVariation(UserDetailsImpl userDetails, String variationId,
                                         ProductVariationUpdateDTO updateDTO, MultipartFile primaryImage,
                                         List<MultipartFile> secondaryImages) throws BadRequestException, ResourceNotFoundException {
        log.info("Updating product variation: {}", variationId);
        ProductVariation variation = productVariationRepository.findById(variationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageSource.getMessage("error.variation.not.found", null, getCurrentLocale())));

        if (!variation.getProduct().getSeller().getEmail().equals(userDetails.getUsername())) {
            throw new BadRequestException(
                    messageSource.getMessage("error.variation.not.authorized", null, getCurrentLocale()));
        }

        Product product = variation.getProduct();
        if (product.isDeleted() || !product.isActive()) {
            throw new BadRequestException(
                    messageSource.getMessage("error.product.inactive", null, getCurrentLocale()));
        }

        if (updateDTO != null) {
            if (updateDTO.getQuantity() != null) {
                variation.setQuantity(updateDTO.getQuantity());
            }
            if (updateDTO.getPrice() != null) {
                variation.setPrice(updateDTO.getPrice());
            }
            if (updateDTO.getActive() != null) {
                variation.setActive(updateDTO.getActive());
            }
            if (updateDTO.getMetadata() != null) {
                validateMetadata(updateDTO.getMetadata(), product.getCategory(), product);
                variation.setMetadata(updateDTO.getMetadata());
            }
        }

        try {
            if (primaryImage != null) {
                fileStorageService.deletePrimaryProductVariationImage(product.getId(), variationId);
                String primaryImageKey = fileStorageService.saveProductVariationImage(
                        product.getId(), variationId, primaryImage);
                variation.setPrimaryImage(primaryImageKey);
            }

            if (secondaryImages != null && !secondaryImages.isEmpty()) {
                fileStorageService.deleteSecondaryProductVariationImages(product.getId(), variationId, secondaryImages.size());
                fileStorageService.saveSecondaryImages(product.getId(), variationId, secondaryImages);
            }
        } catch (IOException e) {
            log.error("Failed to upload images: {}", e.getMessage());
            throw new RuntimeException(messageSource.getMessage("error.failed.upload.images", null, getCurrentLocale()));
        }

        productVariationRepository.save(variation);
        return messageSource.getMessage("success.variation.updated", null, getCurrentLocale());
    }
}