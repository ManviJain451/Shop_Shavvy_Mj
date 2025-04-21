package com.shopsavvy.shopshavvy.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsavvy.shopshavvy.dto.addressDto.AddressDTO;
import com.shopsavvy.shopshavvy.dto.categoryDto.CategoryDTO;
import com.shopsavvy.shopshavvy.dto.categoryDto.CategoryDetailsForSellerDTO;
import com.shopsavvy.shopshavvy.dto.categoryDto.CategoryResponseDTO;
import com.shopsavvy.shopshavvy.dto.productDto.ProductDTO;
import com.shopsavvy.shopshavvy.dto.productDto.ProductUpdateDTO;
import com.shopsavvy.shopshavvy.dto.productDto.ProductVariationDTO;
import com.shopsavvy.shopshavvy.dto.productDto.ProductVariationUpdateDTO;
import com.shopsavvy.shopshavvy.dto.sellerDto.SellerProfileDTO;
import com.shopsavvy.shopshavvy.exception.DuplicateEntryExistsException;
import com.shopsavvy.shopshavvy.exception.UserNotFoundException;
import com.shopsavvy.shopshavvy.model.categories.Category;
import com.shopsavvy.shopshavvy.model.categories.CategoryMetadataFieldValues;
import com.shopsavvy.shopshavvy.model.products.Product;
import com.shopsavvy.shopshavvy.model.products.ProductVariation;
import com.shopsavvy.shopshavvy.model.users.Address;
import com.shopsavvy.shopshavvy.model.users.Seller;
import com.shopsavvy.shopshavvy.repository.*;
import com.shopsavvy.shopshavvy.security.configurations.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jmx.export.metadata.InvalidMetadataException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SellerService {
    private final JwtService jwtService;
    private final SellerRepository sellerRepository;
    private final FileStorageService fileStorageService;
    private final MessageSource messageSource;
    private final CategoryRepository categoryRepository;
    private final CategoryMetadataFieldValuesRepository categoryMetadataFieldValuesRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;
    private final ProductVariationRepository productVariationRepository;

    private Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }


    @Value("${file.storage.base-path}")
    private String basePath;

    public SellerProfileDTO getSellerProfile(UserDetailsImpl userDetailsImpl) {
        Seller seller = sellerRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> new UserNotFoundException(
                        messageSource.getMessage("error.seller.not.found.token", null, getCurrentLocale())));

        String imageUrl = fileStorageService.getUserImageUrl(seller.getId());

        Set<AddressDTO> addressDTOs = seller.getAddresses().stream()
                .map(address -> new AddressDTO(
                        address.getCity(),
                        address.getState(),
                        address.getCountry(),
                        address.getAddressLine(),
                        address.getZipCode(),
                        address.getLabel()
                ))
                .collect(Collectors.toSet());

        return SellerProfileDTO.builder()
                .id(seller.getId())
                .firstName(seller.getFirstName())
                .middleName(seller.getMiddleName())
                .lastName(seller.getLastName())
                .active(seller.getIsActive())
                .companyContact(seller.getCompanyContact())
                .companyName(seller.getCompanyName())
                .gst(seller.getGst())
                .addresses(addressDTOs)
                .imageUrl(imageUrl)
                .build();
    }

    public String updateSellerProfile(UserDetailsImpl userDetailsImpl, SellerProfileDTO sellerProfileDTO) {
        Seller seller = sellerRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> new UserNotFoundException(
                        messageSource.getMessage("error.seller.not.found.token", null, getCurrentLocale())));

        updateSellerFields(seller, sellerProfileDTO);

        if (sellerProfileDTO.getProfileImage() != null && !sellerProfileDTO.getProfileImage().isEmpty()) {
            try {
                fileStorageService.saveOrUpdateUserPhoto(seller.getId(), sellerProfileDTO.getProfileImage());
            } catch (IOException e) {
                throw new RuntimeException(
                        messageSource.getMessage("error.profile.image.upload", null, getCurrentLocale()), e);
            }
        }

        sellerRepository.save(seller);
        return messageSource.getMessage("success.seller.profile.updated", null, getCurrentLocale());
    }

    public String updateAddress(UserDetailsImpl userDetailsImpl, String addressId, AddressDTO addressDTO) throws BadRequestException {
        Seller seller = sellerRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> new UserNotFoundException(
                        messageSource.getMessage("error.seller.not.found.token", null, getCurrentLocale())));

        Address address = seller.getAddresses().stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(
                        messageSource.getMessage("error.address.not.found.id", null, getCurrentLocale())));

        updateAddressFields(address, addressDTO);
        sellerRepository.save(seller);
        return messageSource.getMessage("success.address.updated", null, getCurrentLocale());
    }

    private void updateSellerFields(Seller seller, SellerProfileDTO dto) {
        if (dto.getFirstName() != null) seller.setFirstName(dto.getFirstName());
        if (dto.getMiddleName() != null) seller.setMiddleName(dto.getMiddleName());
        if (dto.getLastName() != null) seller.setLastName(dto.getLastName());
        if (dto.getCompanyContact() != null) seller.setCompanyContact(dto.getCompanyContact());
    }

    private void updateAddressFields(Address address, AddressDTO dto) {
        if (dto.getCity() != null) address.setCity(dto.getCity());
        if (dto.getState() != null) address.setState(dto.getState());
        if (dto.getCountry() != null) address.setCountry(dto.getCountry());
        if (dto.getZipCode() != null) address.setZipCode(dto.getZipCode());
        if (dto.getAddressLine() != null) address.setAddressLine(dto.getAddressLine());
        if (dto.getLabel() != null) address.setLabel(dto.getLabel());
    }

    public List<CategoryDetailsForSellerDTO> viewCategory() {
        return categoryRepository.findAll().stream()
                .filter(category -> category.getSubCategories() == null || category.getSubCategories().isEmpty())
                .map(category -> CategoryDetailsForSellerDTO.builder()
                        .id(category.getCategoryId())
                        .name(category.getName())
                        .parentCategories(category.getParentCategory() != null
                                ? List.of(CategoryResponseDTO.builder()
                                .categoryId(category.getParentCategory().getCategoryId())
                                .categoryName(category.getParentCategory().getName())
                                .parentCategoryId(category.getParentCategory().getParentCategory().getCategoryId())
                                .build())
                                : null)
                        .metadataFieldsWithValues(fetchMetadataFieldsFromRoot(category))
                        .build())
                .collect(Collectors.toList());
    }

    private HashMap<String, String> fetchMetadataFieldsFromRoot(Category category) {
        HashMap<String, String> metadataFieldsWithValues = new HashMap<>();
        while (category != null) {
            categoryMetadataFieldValuesRepository
                    .findMetadataFieldByCategoryId(category.getCategoryId())
                    .forEach(field -> metadataFieldsWithValues.putIfAbsent(
                            field.getCategoryMetadataField().getName(),
                            field.getValues()
                    ));
            category = category.getParentCategory();
        }
        return metadataFieldsWithValues;
    }


    public String addProduct(UserDetailsImpl userDetailsImpl, ProductDTO dto) throws BadRequestException {
        Seller seller = sellerRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> new UserNotFoundException(messageSource
                        .getMessage("error.seller.not.found.toke", null, getCurrentLocale())));
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new BadRequestException(
                        messageSource.getMessage("error.category.not.found", null, getCurrentLocale())));
        if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
            throw new BadRequestException(
                    messageSource.getMessage("error.category.not.leaf.node", null, getCurrentLocale()));
        }

        List<Product> existingProducts = productRepository.findByNameAndCategoryAndBrandAndSeller(
                dto.getProductName(),
                category,
                dto.getBrand(),
                seller
        );

        if (!existingProducts.isEmpty()) {
            throw new BadRequestException(
                    messageSource.getMessage("error.product.already.exists", null, getCurrentLocale()));
        }

        Product product = Product.builder()
                .seller(seller)
                .name(dto.getProductName())
                .description(dto.getDescription() != null ? dto.getDescription() : null)
                .category(category)
                .isCancellable(dto.isCancellable())
                .isReturnable(dto.isReturnable())
                .brand(dto.getBrand())
                .isActive(false)
                .isDeleted(false)
                .build();

        productRepository.save(product);

        Set<Product> existingProductsOfSeller = seller.getProducts();
        existingProductsOfSeller.add(product);
        seller.setProducts(existingProductsOfSeller);
        sellerRepository.save(seller);

        return messageSource.getMessage("success.product.added", null, getCurrentLocale());
    }

    public ProductDTO viewProduct(UserDetailsImpl userDetailsImpl, String productId) throws BadRequestException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BadRequestException(
                        messageSource.getMessage("error.product.not.found", null, getCurrentLocale())));

        if (!product.getSeller().getEmail().equals(userDetailsImpl.getUsername())) {
            throw new BadRequestException(
                    messageSource.getMessage("error.product.not.authorized", null, getCurrentLocale()));
        }

        if (product.isDeleted()) {
            throw new BadRequestException(
                    messageSource.getMessage("error.product.deleted", null, getCurrentLocale()));
        }

        CategoryDTO categoryDTO = CategoryDTO.builder()
                .id(product.getCategory().getCategoryId())
                .name(product.getCategory().getName())
                .build();

        return ProductDTO.builder()
                .productId(product.getId())
                .productName(product.getName())
                .description(product.getDescription())
                .brand(product.getBrand())
                .active(product.isActive())
                .cancellable(product.isCancellable())
                .returnable(product.isReturnable())
                .categoryDetails(categoryDTO)
                .build();
    }

    public List<ProductDTO> viewAllProducts(UserDetailsImpl userDetails){
        Seller seller = sellerRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException(messageSource
                        .getMessage("user.not.found", null, getCurrentLocale())));

        List<Product> products = productRepository.findBySeller(seller);
        return products
                .stream()
                .map(product -> ProductDTO.builder()
                        .productId(product.getId())
                        .productName(product.getName())
                        .description(product.getDescription())
                        .brand(product.getBrand())
                        .active(product.isActive())
                        .cancellable(product.isCancellable())
                        .returnable(product.isReturnable())
                        .categoryDetails(CategoryDTO.builder()
                                .id(product.getCategory().getCategoryId())
                                .name(product.getCategory().getName())
                                .build())
                        .build())
                .collect(Collectors.toList());
    }


    public String addProductVariations(ProductVariationDTO dto, MultipartFile primaryImage, List<MultipartFile> secondaryImages) throws BadRequestException {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new BadRequestException(messageSource.getMessage("product.not.found", new Object[]{dto.getProductId()}, getCurrentLocale())));
        if (product.isDeleted()) {
            throw new BadRequestException(messageSource.getMessage("product.deleted", null, getCurrentLocale()));
        }
        if (!product.isActive()) {
            throw new BadRequestException(messageSource.getMessage("product.inactive", null, getCurrentLocale()));
        }
        Category category = product.getCategory();

        boolean alreadyExists = product.getProductVariations().stream()
                .anyMatch(v -> Objects.equals(v.getMetadata(), dto.getMetadata()));
        if (alreadyExists)
            throw new DuplicateEntryExistsException(messageSource.getMessage("product.variation.duplicate", null, getCurrentLocale()));

        validateMetadata(dto.getMetadata(), category, product);

        ProductVariation variation = new ProductVariation();
        variation.setProduct(product);
        variation.setMetadata(dto.getMetadata());
        variation.setQuantity(dto.getQuantity());
        variation.setPrice(dto.getPrice());
        variation.setActive(true);

        ProductVariation savedVariation = productVariationRepository.save(variation);

        try {
            System.out.println(primaryImage);
            String primaryImageKey = fileStorageService.saveProductVariationImage(product.getId(), savedVariation.getId(), primaryImage);
            System.out.println(primaryImageKey);
            savedVariation.setPrimaryImage(primaryImageKey);
            System.out.println(savedVariation.getPrimaryImage());

            if (secondaryImages != null && !secondaryImages.isEmpty()) {
                fileStorageService.saveSecondaryImages(product.getId(), savedVariation.getId(), secondaryImages);
            }

            productVariationRepository.save(savedVariation);

        } catch (IOException e) {
            productVariationRepository.delete(savedVariation);
            throw new RuntimeException("Failed to upload images: " + e.getMessage());
        }

        return "Product variation created";

    }

    private void validateMetadata(Map<String, Object> metadata, Category category, Product product) throws BadRequestException {
        if (metadata == null || metadata.isEmpty()) {
            throw new BadRequestException(messageSource.getMessage("metadata.min.one", null, getCurrentLocale()));
        }

        Set<String> newMetadataFields = metadata.keySet();

        if (!product.getProductVariations().isEmpty()) {
            Optional<ProductVariation> existingVariation = product.getProductVariations().stream()
                    .filter(v -> v.getMetadata() != null && !v.getMetadata().isEmpty())
                    .findFirst();

            if (existingVariation.isPresent()) {
                Set<String> existingFields = existingVariation.get().getMetadata().keySet();

                if (!existingFields.equals(newMetadataFields)) {
                    throw new InvalidMetadataException(messageSource.getMessage("metadata.structure.mismatch", null, getCurrentLocale()));
                }
            }
        }

        Map<String, Set<String>> existingMetadata = new HashMap<>();
        Category current = category;

        while (current != null) {
            System.out.println(current.getName());
            List<CategoryMetadataFieldValues> metadataValues = categoryMetadataFieldValuesRepository.findByCategory(current);
            for (CategoryMetadataFieldValues value : metadataValues) {
                String fieldName = value.getCategoryMetadataField().getName();
                Set<String> allowedValues = Arrays.stream(value.getValues().split(",")).map(String::trim).collect(Collectors.toSet());
                existingMetadata.put(fieldName, allowedValues);
            }
            current = current.getParentCategory();
        }
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            String fieldName = entry.getKey();
            String value = entry.getValue().toString();
            System.out.println("value = " + value);
            System.out.println("existingValue = " + existingMetadata.get(fieldName));

            if (!existingMetadata.containsKey(fieldName))
                throw new InvalidMetadataException(messageSource.getMessage("metadata.field.invalid", new Object[]{fieldName}, getCurrentLocale()));
            if (!existingMetadata.get(fieldName).contains(value))
                throw new InvalidMetadataException(messageSource.getMessage("metadata.value.invalid", new Object[]{value, fieldName}, getCurrentLocale()));
        }
    }

    public ProductVariationDTO viewProductVariation(UserDetailsImpl userDetails, String productVariationId) throws BadRequestException {
        ProductVariation variation = productVariationRepository.findById(productVariationId)
                .orElseThrow(() -> new BadRequestException(
                        messageSource.getMessage("error.variation.not.found", null, getCurrentLocale())));

        if (!variation.getProduct().getSeller().getEmail().equals(userDetails.getUsername())) {
            throw new BadRequestException(
                    messageSource.getMessage("error.variation.not.authorized", null, getCurrentLocale()));
        }

        if (variation.getProduct().isDeleted()) {
            throw new BadRequestException(
                    messageSource.getMessage("error.product.deleted", null, getCurrentLocale()));
        }

        ProductDTO parentProduct = ProductDTO.builder()
                .productId(variation.getProduct().getId())
                .productName(variation.getProduct().getName())
                .brand(variation.getProduct().getBrand())
                .description(variation.getProduct().getDescription())
                .active(variation.getProduct().isActive())
                .cancellable(variation.getProduct().isCancellable())
                .returnable(variation.getProduct().isReturnable())
                .categoryDetails(CategoryDTO.builder()
                        .id(variation.getProduct().getCategory().getCategoryId())
                        .name(variation.getProduct().getCategory().getName())
                        .build())
                .build();

        return ProductVariationDTO.builder()
                .productId(variation.getProduct().getId())
                .price(variation.getPrice())
                .quantity(variation.getQuantity())
                .metadata(variation.getMetadata())
                .parentProduct(parentProduct)
                .build();
    }

    public List<ProductVariationDTO> viewProductVariations(UserDetailsImpl userDetails, String productId) throws BadRequestException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BadRequestException(
                        messageSource.getMessage("error.product.not.found", null, getCurrentLocale())));

        if (!product.getSeller().getEmail().equals(userDetails.getUsername())) {
            throw new BadRequestException(
                    messageSource.getMessage("error.product.not.authorized", null, getCurrentLocale()));
        }

        if (product.isDeleted()) {
            throw new BadRequestException(
                    messageSource.getMessage("error.product.deleted", null, getCurrentLocale()));
        }

        return product.getProductVariations().stream()
                .map(variation -> ProductVariationDTO.builder()
                        .productId(product.getId())
                        .price(variation.getPrice())
                        .quantity(variation.getQuantity())
                        .metadata(variation.getMetadata())
                        .build())
                .collect(Collectors.toList());
    }

    public String deleteProduct(UserDetailsImpl userDetails, String productId) throws BadRequestException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BadRequestException(
                        messageSource.getMessage("error.product.not.found", null, getCurrentLocale())));

        if (!product.getSeller().getEmail().equals(userDetails.getUsername())) {
            throw new BadRequestException(
                    messageSource.getMessage("error.product.not.authorized", null, getCurrentLocale()));
        }
        if(product.isDeleted()){
            throw new BadRequestException(
                    messageSource.getMessage("error.product.already.deleted", null, getCurrentLocale()));

        }

        product.setDeleted(true);
        productRepository.save(product);

        return messageSource.getMessage("success.product.deleted", null, getCurrentLocale());
    }

    public String updateProduct(UserDetailsImpl userDetails, String productId, ProductUpdateDTO updateDTO) throws BadRequestException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BadRequestException(
                        messageSource.getMessage("error.product.not.found", null, getCurrentLocale())));

        if (!product.getSeller().getEmail().equals(userDetails.getUsername())) {
            throw new BadRequestException(
                    messageSource.getMessage("error.product.not.authorized", null, getCurrentLocale()));
        }

        if (product.isDeleted()) {
            throw new BadRequestException(
                    messageSource.getMessage("error.product.deleted", null, getCurrentLocale()));
        }

        if (updateDTO.getName() != null && !updateDTO.getName().equals(product.getName())) {
            boolean exists = productRepository.existsByNameAndBrandAndCategoryAndSellerAndIdNot(
                    updateDTO.getName(),
                    product.getBrand(),
                    product.getCategory(),
                    product.getSeller(),
                    productId
            );
            if (exists) {
                throw new BadRequestException(
                        messageSource.getMessage("error.product.name.exists", null, getCurrentLocale()));
            }
            product.setName(updateDTO.getName());
        }

        if (updateDTO.getDescription() != null) {
            product.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getCancellable() != null) {
            product.setCancellable(updateDTO.getCancellable());
        }
        if (updateDTO.getReturnable() != null) {
            product.setReturnable(updateDTO.getReturnable());
        }

        productRepository.save(product);
        return messageSource.getMessage("success.product.updated", null, getCurrentLocale());
    }

    public String updateProductVariation(UserDetailsImpl userDetails, String variationId,
                                         ProductVariationUpdateDTO updateDTO, MultipartFile primaryImage,
                                         List<MultipartFile> secondaryImages) throws BadRequestException {

        ProductVariation variation = productVariationRepository.findById(variationId)
                .orElseThrow(() -> new BadRequestException(
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
                fileStorageService.deleteProductVariationImages(product.getId(), variationId);
                String primaryImageKey = fileStorageService.saveProductVariationImage(
                        product.getId(), variationId, primaryImage);
                variation.setPrimaryImage(primaryImageKey);
            }

            if (secondaryImages != null && !secondaryImages.isEmpty()) {
                fileStorageService.deleteProductVariationImages(product.getId(), variationId);
                fileStorageService.saveSecondaryImages(product.getId(), variationId, secondaryImages);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to update images: " + e.getMessage());
        }

        productVariationRepository.save(variation);
        return messageSource.getMessage("success.variation.updated", null, getCurrentLocale());
    }

}

