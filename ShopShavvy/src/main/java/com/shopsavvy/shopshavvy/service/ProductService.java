package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.configuration.UserDetailsImpl;
import com.shopsavvy.shopshavvy.dto.categoryDto.CategoryDTO;
import com.shopsavvy.shopshavvy.dto.productDto.*;
import com.shopsavvy.shopshavvy.exception.UserNotFoundException;
import com.shopsavvy.shopshavvy.model.categories.Category;
import com.shopsavvy.shopshavvy.model.products.Product;
import com.shopsavvy.shopshavvy.model.products.ProductVariation;
import com.shopsavvy.shopshavvy.model.users.Seller;
import com.shopsavvy.shopshavvy.repository.*;
import com.shopsavvy.shopshavvy.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final MessageSource messageSource;
    private final ProductRepository productRepository;
    private final EmailService emailService;
    private final FileStorageService fileStorageService;
    private final SellerRepository sellerRepository;
    private final CategoryRepository categoryRepository;
    private Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    //admin
    public ProductDTO viewProduct(String productId) throws BadRequestException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BadRequestException(
                        messageSource.getMessage("error.product.not.found", null, getCurrentLocale())));

        Set<ProductVariationResponseDTO> variations = product.getProductVariations().stream()
                .map(variation -> {
                    String imageUrl = null;
                    if (variation.getPrimaryImage() != null) {
                        imageUrl = fileStorageService.getProductVariationImageUrl(
                                product.getId(),
                                variation.getId(),
                                variation.getPrimaryImage());
                    }

                    return ProductVariationResponseDTO.builder()
                            .price(variation.getPrice())
                            .quantity(variation.getQuantity())
                            .metadata(variation.getMetadata())
                            .primaryImage(imageUrl)
                            .build();
                })
                .collect(Collectors.toSet());

        return ProductDTO.builder()
                .productId(product.getId())
                .productName(product.getName())
                .sellerId(product.getSeller().getId())
                .brand(product.getBrand())
                .description(product.getDescription())
                .active(product.isActive())
                .cancellable(product.isCancellable())
                .returnable(product.isReturnable())
                .categoryDetails(CategoryDTO.builder()
                        .id(product.getCategory().getCategoryId())
                        .name(product.getCategory().getName())
                        .build())
                .productVariations(variations)
                .build();
    }

    public String toggleProductStatus(String productId) throws BadRequestException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BadRequestException(
                        messageSource.getMessage("error.product.not.found", null, getCurrentLocale())));

        if (product.isDeleted()) {
            throw new BadRequestException(messageSource.getMessage("error.deleted.product", null, getCurrentLocale()));
        }

        boolean currentlyActive = product.isActive();

        product.setActive(!currentlyActive);
        productRepository.save(product);

        emailService.sendProductStatusUpdateEmail(
                product.getSeller().getEmail(),
                product.getName(),
                !currentlyActive,
                product.getBrand(),
                product.getDescription());

        return messageSource.getMessage(
                !currentlyActive ? "success.product.activated" : "success.product.deactivated",
                null,
                getCurrentLocale());
    }

    public List<ProductDTO> viewAllProducts(String sort, String order, int max, int offset, Map<String, String> filter) {
        Pageable pageable = PageRequest.of(offset / max, max,
                Sort.by(Sort.Direction.fromString(order.toUpperCase()), sort));

        Specification<Product> specification = ProductSpecification.getAllByFilterMap(filter);

        Page<Product> productsPage = productRepository.findAll(specification, pageable);

        return productsPage.getContent().stream()
                .map(product -> {
                    Set<ProductVariationResponseDTO> variations = product.getProductVariations().stream()
                            .filter(ProductVariation::isActive)
                            .map(variation -> ProductVariationResponseDTO.builder()
                                    .productVariationId(variation.getId())
                                    .price(variation.getPrice())
                                    .quantity(variation.getQuantity())
                                    .metadata(variation.getMetadata())
                                    .primaryImage(variation.getPrimaryImage() != null ?
                                            fileStorageService.getProductVariationImageUrl(
                                                    product.getId(), variation.getId(), variation.getPrimaryImage()) : null)
                                    .build()
                            )
                            .collect(Collectors.toSet());

                    return ProductDTO.builder()
                            .productId(product.getId())
                            .productName(product.getName())
                            .sellerId(product.getSeller().getId())
                            .brand(product.getBrand())
                            .description(product.getDescription())
                            .active(product.isActive())
                            .cancellable(product.isCancellable())
                            .returnable(product.isReturnable())
                            .categoryDetails(CategoryDTO.builder()
                                    .id(product.getCategory().getCategoryId())
                                    .name(product.getCategory().getName())
                                    .build())
                            .productVariations(variations)
                            .build();
                })
                .toList();
    }

    //customer
    public ProductDTO viewProductCustomer(String productId) throws BadRequestException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BadRequestException(
                        messageSource.getMessage("error.product.not.found", null, getCurrentLocale())));

        if (product.isDeleted()) {
            throw new BadRequestException(
                    messageSource.getMessage("error.product.deleted", null, getCurrentLocale()));
        }
        if (!product.isActive()) {
            throw new BadRequestException(
                    messageSource.getMessage("error.product.deactive", null, getCurrentLocale()));
        }
        if(product.getProductVariations().isEmpty()){
            throw new BadRequestException(
                    messageSource.getMessage("error.product.doesnt.have.variations", null, getCurrentLocale()));
        }

        Set<ProductVariationResponseDTO> variations = product.getProductVariations().stream()
                .filter(ProductVariation::isActive)
                .map(variation -> {
                    String imageUrl = null;
                    if (variation.getPrimaryImage() != null) {
                        imageUrl = fileStorageService.getProductVariationImageUrl(
                                product.getId(),
                                variation.getId(),
                                variation.getPrimaryImage());
                    }
                    List<String> secondaryImagesUrl = fileStorageService.getProductVariationSecondaryImageUrls(
                            product.getId(), variation.getId(), variation.getPrimaryImage());
                    return ProductVariationResponseDTO.builder()
                            .productVariationId(variation.getId())
                            .price(variation.getPrice())
                            .quantity(variation.getQuantity())
                            .metadata(variation.getMetadata())
                            .primaryImage(imageUrl)
                            .secondaryImages(secondaryImagesUrl)
                            .build();
                })
                .collect(Collectors.toSet());

        return ProductDTO.builder()
                .productId(product.getId())
                .productName(product.getName())
                .brand(product.getBrand())
                .description(product.getDescription())
                .active(product.isActive())
                .cancellable(product.isCancellable())
                .returnable(product.isReturnable())
                .categoryDetails(CategoryDTO.builder()
                        .id(product.getCategory().getCategoryId())
                        .name(product.getCategory().getName())
                        .build())
                .productVariations(variations)
                .sellerId(product.getSeller().getId())
                .build();
    }

    public List<ProductDTO> viewAllProducts(String categoryId, String sort, String order, int max, int offset, Map<String, String> filter) throws BadRequestException {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BadRequestException(messageSource
                        .getMessage("error.category.not.found", null, getCurrentLocale())));

        Pageable pageable = PageRequest.of(offset / max, max,
                Sort.by(Sort.Direction.fromString(order.toUpperCase()), sort));

        List<Product> products = new ArrayList<>();

        if (category.getSubCategories() == null || category.getSubCategories().isEmpty()) {
            Specification<Product> specification = ProductSpecification.getAllByFilterWithCategory(filter, category);
            Page<Product> page = productRepository.findAll(specification, pageable);
            products.addAll(page.getContent());
        } else {
            for (Category subCategory : category.getSubCategories()) {
                Specification<Product> specification = ProductSpecification.getAllByFilterWithCategory(filter, subCategory);
                Page<Product> page = productRepository.findAll(specification, pageable);
                products.addAll(page.getContent());
            }
        }

        return products.stream()
                .map(product -> {
                    Set<ProductVariationResponseDTO> variations = product.getProductVariations().stream()
                            .filter(ProductVariation::isActive)
                            .map(variation -> ProductVariationResponseDTO.builder()
                                    .productVariationId(variation.getId())
                                    .price(variation.getPrice())
                                    .quantity(variation.getQuantity())
                                    .metadata(variation.getMetadata())
                                    .primaryImage(variation.getPrimaryImage() != null ?
                                            fileStorageService.getProductVariationImageUrl(
                                                    product.getId(), variation.getId(), variation.getPrimaryImage()) : null)
                                    .secondaryImages(fileStorageService.getProductVariationSecondaryImageUrls(product.getId(), variation.getId(), variation.getPrimaryImage()))
                                    .build()
                            )
                            .collect(Collectors.toSet());

                    return ProductDTO.builder()
                            .productId(product.getId())
                            .productName(product.getName())
                            .sellerId(product.getSeller().getId())
                            .brand(product.getBrand())
                            .description(product.getDescription())
                            .active(product.isActive())
                            .cancellable(product.isCancellable())
                            .returnable(product.isReturnable())
                            .categoryDetails(CategoryDTO.builder()
                                    .id(product.getCategory().getCategoryId())
                                    .name(product.getCategory().getName())
                                    .build())
                            .productVariations(variations)
                            .build();
                })
                .toList();
    }

    public List<ProductDTO> viewSimilarProducts(String productId, String sort, String order, int max, int offset, Map<String, String> filter) throws BadRequestException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BadRequestException(messageSource
                        .getMessage("error.product.not.found", null, getCurrentLocale())));

        Pageable pageable = PageRequest.of(offset / max, max,
                Sort.by(Sort.Direction.fromString(order.toUpperCase()), sort));

        Specification<Product> specification = ProductSpecification.getAllByFilterWithCategory(filter, product.getCategory());

        Page<Product> page = productRepository.findAll(specification, pageable);

        return page.getContent().stream()
                .map(prod -> {
                    Set<ProductVariationResponseDTO> variations = prod.getProductVariations().stream()
                            .filter(ProductVariation::isActive)
                            .map(variation -> ProductVariationResponseDTO.builder()
                                    .productVariationId(variation.getId())
                                    .price(variation.getPrice())
                                    .quantity(variation.getQuantity())
                                    .metadata(variation.getMetadata())
                                    .primaryImage(variation.getPrimaryImage() != null ?
                                            fileStorageService.getProductVariationImageUrl(
                                                    product.getId(), variation.getId(), variation.getPrimaryImage()) : null)
                                    .build()
                            )
                            .collect(Collectors.toSet());

                    return ProductDTO.builder()
                            .productId(prod.getId())
                            .productName(prod.getName())
                            .sellerId(prod.getSeller().getId())
                            .brand(prod.getBrand())
                            .description(prod.getDescription())
                            .active(prod.isActive())
                            .cancellable(prod.isCancellable())
                            .returnable(prod.isReturnable())
                            .categoryDetails(CategoryDTO.builder()
                                    .id(prod.getCategory().getCategoryId())
                                    .name(prod.getCategory().getName())
                                    .build())
                            .productVariations(variations)
                            .build();
                })
                .toList();

    }


    //seller
    public String addProduct(UserDetailsImpl userDetailsImpl, ProductDTO dto) throws BadRequestException {
        Seller seller = sellerRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> new UserNotFoundException(messageSource
                        .getMessage("error.seller.not.found.token", null, getCurrentLocale())));
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

        return mapProductToProductDTO(product);
    }

    public List<ProductDTO> viewAllProducts(UserDetailsImpl userDetails, String sort, String order, int max, int offset, Map<String, String> filter ){
        Seller seller = sellerRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException(messageSource
                        .getMessage("user.not.found", null, getCurrentLocale())));

        Pageable pageable = PageRequest.of(offset / max, max,
                Sort.by(Sort.Direction.fromString(order.toUpperCase()), sort));

        Specification<Product> specification = ProductSpecification.getAllByFilterWithSeller(filter, seller);
        Page<Product> productsPage = productRepository.findAll(specification, pageable);

        return productsPage.getContent()
                .stream()
                .map(this::mapProductToProductDTO)
                .toList();
    }

    public String deleteProduct(UserDetailsImpl userDetails, String productId) throws BadRequestException {
        Product product = getProductAndValidateWithSeller(userDetails, productId);
        product.setDeleted(true);
        productRepository.save(product);

        return messageSource.getMessage("success.product.deleted", null, getCurrentLocale());
    }

    public String updateProduct(UserDetailsImpl userDetails, String productId, ProductUpdateDTO updateDTO) throws BadRequestException {
        Product product = getProductAndValidateWithSeller(userDetails, productId);
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

    private ProductDTO mapProductToProductDTO(Product product){
        return ProductDTO.builder()
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
                .build();
    }

    private Product getProductAndValidateWithSeller(UserDetailsImpl userDetails, String productId) throws BadRequestException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BadRequestException(
                        messageSource.getMessage("error.product.not.found", null, getCurrentLocale())));

        if (!product.getSeller().getEmail().equals(userDetails.getUsername())) {
            throw new BadRequestException(
                    messageSource.getMessage("error.product.not.authorized", null, getCurrentLocale()));
        }
        return product;
    }

}
