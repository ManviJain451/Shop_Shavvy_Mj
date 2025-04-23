package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.dto.addressDto.AddressDTO;
import com.shopsavvy.shopshavvy.dto.addressDto.CustomerAddressDTO;
import com.shopsavvy.shopshavvy.dto.categoryDto.CategoryDTO;
import com.shopsavvy.shopshavvy.dto.categoryDto.FilteringDetailsDTO;
import com.shopsavvy.shopshavvy.dto.customerDto.CustomerProfileDTO;
import com.shopsavvy.shopshavvy.dto.productDto.ProductDTO;
import com.shopsavvy.shopshavvy.dto.productDto.ProductFilterDTO;
import com.shopsavvy.shopshavvy.dto.productDto.ProductVariationDTO;
import com.shopsavvy.shopshavvy.dto.productDto.ProductVariationResponseDTO;
import com.shopsavvy.shopshavvy.dto.sellerDto.SellerResponseDTO;
import com.shopsavvy.shopshavvy.exception.ResourceNotFoundException;
import com.shopsavvy.shopshavvy.exception.UserNotFoundException;
import com.shopsavvy.shopshavvy.model.categories.Category;
import com.shopsavvy.shopshavvy.model.products.Product;
import com.shopsavvy.shopshavvy.model.products.ProductVariation;
import com.shopsavvy.shopshavvy.model.users.Address;
import com.shopsavvy.shopshavvy.model.users.Customer;
import com.shopsavvy.shopshavvy.repository.*;
import com.shopsavvy.shopshavvy.security.configurations.UserDetailsImpl;
import com.shopsavvy.shopshavvy.specification.ProductSpecification;
import com.shopsavvy.shopshavvy.utilities.StringToDtoParser;
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
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {
    private final JwtService jwtService;
    private final CustomerRepository customerRepository;
    private final FileStorageService fileStorageService;
    private final AddressRepository addressRepository;
    private final MessageSource messageSource;
    private final CategoryRepository categoryRepository;
    private final CategoryMetadataFieldValuesRepository categoryMetadataFieldValuesRepository;
    private final ProductRepository productRepository;

    private Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    public CustomerProfileDTO getCustomerProfile(UserDetailsImpl userDetailsImpl) {

        Customer customer = customerRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.custoomer.not.found", null, getCurrentLocale())));

        String imageUrl = fileStorageService.getUserImageUrl(customer.getId());
        return CustomerProfileDTO.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .middleName(customer.getMiddleName())
                .lastName(customer.getLastName())
                .active(customer.getIsActive())
                .contact(customer.getContact())
                .imageUrl(imageUrl)
                .build();

    }

    public List<AddressDTO> getCustomerAddresses(UserDetailsImpl userDetailsImpl) {
        Customer customer = customerRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.customer.not.found.token", null, getCurrentLocale())));

        return customer.getAddresses()
                .stream()
                .filter(address -> !address.isDeleted())
                .map(address -> new AddressDTO(
                        address.getCity(),
                        address.getState(),
                        address.getCountry(),
                        address.getAddressLine(),
                        address.getLabel(),
                        address.getZipCode()
                ))
                .collect(Collectors.toList());
    }

    public String updateCustomerProfile(UserDetailsImpl userDetailsImpl, CustomerProfileDTO customerProfileDTO) throws IOException {
        Customer customer = customerRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.customer.not.found.token", null, getCurrentLocale())));


        if (customerProfileDTO.getFirstName() != null) {
            customer.setFirstName(customerProfileDTO.getFirstName());
        }
        if (customerProfileDTO.getLastName() != null) {
            customer.setLastName(customerProfileDTO.getLastName());
        }
        if (customerProfileDTO.getMiddleName() != null) {
            customer.setMiddleName(customerProfileDTO.getMiddleName());
        }
        if (customerProfileDTO.getContact() != null) {
            customer.setContact(customerProfileDTO.getContact());
        }

        if (customerProfileDTO.getProfileImage() != null && !customerProfileDTO.getProfileImage().isEmpty()) {
            fileStorageService.saveOrUpdateUserPhoto(customer.getId(), customerProfileDTO.getProfileImage());

        }

        customerRepository.save(customer);
        return messageSource.getMessage("success.profile.updated", null, getCurrentLocale());
    }

    public String addCustomerAddress(UserDetailsImpl userDetailsImpl, CustomerAddressDTO customerAddressDTO) {
        Customer customer = customerRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.customer.not.found.token", null, getCurrentLocale())));
        Address newAddress = Address.builder()
                .city(customerAddressDTO.getCity())
                .state(customerAddressDTO.getState())
                .country(customerAddressDTO.getCountry())
                .addressLine(customerAddressDTO.getAddressLine())
                .zipCode(customerAddressDTO.getZipCode())
                .label(customerAddressDTO.getLabel())
                .build();


        addressRepository.save(newAddress);
        customer.getAddresses().add(newAddress);
        if (customerAddressDTO.isMakeDefault() || customer.getAddresses().size() == 1) {
            customer.setDefaultAddressId(newAddress.getId());
        }

        customerRepository.save(customer);
        return messageSource.getMessage("success.address.added", null, getCurrentLocale());
    }

    public String deleteCustomerAddress(UserDetailsImpl userDetailsImpl, String addressId) throws BadRequestException {
        Customer customer = customerRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.customer.not.found.token", null, getCurrentLocale())));

        customer.getAddresses().stream()
                .filter(a -> a.getId().equals(addressId) && !a.isDeleted())
                .findFirst()
                .orElseThrow(() -> new BadRequestException(messageSource.getMessage("error.address.not.found", null, getCurrentLocale())));

        if (addressId.equals(customer.getDefaultAddressId())) {
            Address newDefaultAddress = customer.getAddresses().stream()
                    .filter(a -> !a.getId().equals(addressId))
                    .findFirst()
                    .orElse(null);
            customer.setDefaultAddressId(newDefaultAddress != null ? newDefaultAddress.getId() : null);
        }

        addressRepository.deleteById(addressId);
        customerRepository.save(customer);
        return messageSource.getMessage("success.address.deleted", null, getCurrentLocale());
    }

    public String updateCustomerAddress(UserDetailsImpl userDetailsImpl, String addressId, CustomerAddressDTO customerAddressDTO) throws BadRequestException {
        Customer customer = customerRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.customer.not.found", null, getCurrentLocale())));

        Address addressToUpdate = customer.getAddresses().stream()
                .filter(addr -> addr.getId().equals(addressId) && !addr.isDeleted())
                .findFirst()
                .orElseThrow(() -> new BadRequestException(messageSource.getMessage("error.address.not.found.id", null, getCurrentLocale())));

        if (customerAddressDTO.getCity() != null) addressToUpdate.setCity(customerAddressDTO.getCity());
        if (customerAddressDTO.getState() != null) addressToUpdate.setState(customerAddressDTO.getState());
        if (customerAddressDTO.getCountry() != null) addressToUpdate.setCountry(customerAddressDTO.getCountry());
        if (customerAddressDTO.getAddressLine() != null) addressToUpdate.setAddressLine(customerAddressDTO.getAddressLine());
        if (customerAddressDTO.getZipCode() != null) addressToUpdate.setZipCode(customerAddressDTO.getZipCode());
        if (customerAddressDTO.getLabel() != null) addressToUpdate.setLabel(customerAddressDTO.getLabel());

        if (customerAddressDTO.isMakeDefault()) {
            customer.setDefaultAddressId(addressToUpdate.getId());
        }

        addressRepository.save(addressToUpdate);
        customerRepository.save(customer);
        return messageSource.getMessage("success.address.updated", null, getCurrentLocale());
    }

    public List<CategoryDTO> viewAllCategories(String categoryId) throws BadRequestException {
        if(categoryId == null || categoryId.isBlank()) {
            return categoryRepository.findByParentCategoryIsNull()
                    .stream()
                    .map(category -> CategoryDTO.builder()
                            .name(category.getName())
                            .id(category.getCategoryId())
                            .build())
                    .collect(Collectors.toList());
        }
        if (!categoryRepository.existsById(categoryId)) {
            throw new BadRequestException(messageSource.getMessage("error.category.doesnt.exist", null, getCurrentLocale()));
        }

        Set<Category> childCategories = categoryRepository.findById(categoryId).get().getSubCategories();
        return childCategories.stream()
                .map(category -> CategoryDTO.builder()
                        .id(category.getCategoryId())
                        .name(category.getName())
                        .build())
                .collect(Collectors.toList());


    }

    public FilteringDetailsDTO getFilteringDetails(String categoryId) throws BadRequestException {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BadRequestException(messageSource
                        .getMessage("error.category.doesnt.exist", null, getCurrentLocale())));

        HashMap<String, String> metadataFieldsWithValues = fetchMetadataFieldsFromRoot(category);

        Set<Category> allCategories = getAllSubCategories(category);
        List<Product> products = productRepository.findByCategoryIn(allCategories);

        List<String> brands = products.stream()
                .map(Product::getBrand)
                .distinct()
                .collect(Collectors.toList());

        Double minPrice = products.stream()
                .flatMap(product -> product.getProductVariations().stream())
                .mapToDouble(ProductVariation::getPrice)
                .min()
                .orElse(0.0);

        Double maxPrice = products.stream()
                .flatMap(product -> product.getProductVariations().stream())
                .mapToDouble(ProductVariation::getPrice)
                .max()
                .orElse(0.0);

        return FilteringDetailsDTO.builder()
                .id(categoryId)
                .categoryName(category.getName())
                .metadataFields(metadataFieldsWithValues)
                .brands(brands)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .build();
    }

    private Set<Category> getAllSubCategories(Category category) {
        Set<Category> allCategories = new HashSet<>();
        allCategories.add(category);
        if (category.getSubCategories() != null) {
            for (Category subCategory : category.getSubCategories()) {
                allCategories.addAll(getAllSubCategories(subCategory));
            }
        }
        return allCategories;
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

    public ProductDTO viewProduct(String productId) throws BadRequestException {
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

    public List<ProductDTO> viewAllProducts(String categoryId, String sort, String order, int max, int offset, String query) throws BadRequestException {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BadRequestException(messageSource
                        .getMessage("error.category.not.found", null, getCurrentLocale())));

        Pageable pageable = PageRequest.of(offset / max, max,
                Sort.by(Sort.Direction.fromString(order.toUpperCase()), sort));


        ProductFilterDTO filterDTO = StringToDtoParser.parseQueryToFilterDTO(query);

        List<Product> products = new ArrayList<>();

        if (category.getSubCategories() == null || category.getSubCategories().isEmpty()) {
            Specification<Product> specification = ProductSpecification.getAllByFilterWithCategory(filterDTO, category);
            Page<Product> page = productRepository.findAll(specification, pageable);
            products.addAll(page.getContent());
        } else {
            for (Category subCategory : category.getSubCategories()) {
                Specification<Product> specification = ProductSpecification.getAllByFilterWithCategory(filterDTO, subCategory);
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
                .collect(Collectors.toList());
    }

    public List<ProductDTO> viewSimilarProducts(String productId, String sort, String order, int max, int offset, String query) throws BadRequestException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BadRequestException(messageSource
                        .getMessage("error.product.not.found", null, getCurrentLocale())));

        Pageable pageable = PageRequest.of(offset / max, max,
                Sort.by(Sort.Direction.fromString(order.toUpperCase()), sort));

        ProductFilterDTO filterDTO = StringToDtoParser.parseQueryToFilterDTO(query);

        Specification<Product> specification = ProductSpecification.getAllByFilterWithCategory(filterDTO, product.getCategory());

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
                .collect(Collectors.toList());

    }



    }
