package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.dto.category_dto.*;
import com.shopsavvy.shopshavvy.exception.DuplicateEntryExistsException;
import com.shopsavvy.shopshavvy.exception.ResourceNotFoundException;
import com.shopsavvy.shopshavvy.model.categories.Category;
import com.shopsavvy.shopshavvy.model.categories.CategoryMetadataField;
import com.shopsavvy.shopshavvy.model.categories.CategoryMetadataFieldValueId;
import com.shopsavvy.shopshavvy.model.categories.CategoryMetadataFieldValues;
import com.shopsavvy.shopshavvy.model.products.Product;
import com.shopsavvy.shopshavvy.model.products.ProductVariation;
import com.shopsavvy.shopshavvy.repository.CategoryMetadataFieldRepository;
import com.shopsavvy.shopshavvy.repository.CategoryMetadataFieldValuesRepository;
import com.shopsavvy.shopshavvy.repository.CategoryRepository;
import com.shopsavvy.shopshavvy.repository.ProductRepository;
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
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CategoryService {

    private final MessageSource messageSource;
    private final CategoryMetadataFieldRepository categoryMetadataFieldRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMetadataFieldValuesRepository categoryMetadataFieldValuesRepository;
    private Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    public String addMetadataField(String fieldName) {
        log.info("Attempting to add metadata field: {}", fieldName);
        fieldName = fieldName.trim();
        if (categoryMetadataFieldRepository.existsByName(fieldName)) {
            log.warn("Metadata field with name '{}' already exists", fieldName);
            throw new DuplicateEntryExistsException(messageSource.getMessage("error.field.already.exists", null, getCurrentLocale()));
        }
        CategoryMetadataField categoryMetadataField = CategoryMetadataField.builder()
                .name(fieldName).build();
        categoryMetadataFieldRepository.save(categoryMetadataField);
        log.info("Successfully created metadata field with ID: {}", categoryMetadataField.getId());
        return messageSource.getMessage("success.created.metadata.field", new Object[]{categoryMetadataField.getId()}, getCurrentLocale());
    }

    public List<CategoryMetadataField> getAllMetadataFields(int max, int offset, String sortBy, String order, String query) {
        log.info("Retrieving metadata fields with parameters - max: {}, offset: {}, sortBy: {}, order: {}, query: {}",
                max, offset, sortBy, order, query);
        Sort sort = Sort.by(Sort.Order.by(sortBy).ignoreCase().with(Sort.Direction.valueOf(order.toUpperCase())));
        Pageable pageable = PageRequest.of(offset, max, sort);

        Page<CategoryMetadataField> metadataFields = (query != null && !query.isEmpty()) ?
                categoryMetadataFieldRepository.findByNameContainingIgnoreCase(query, pageable) :
                categoryMetadataFieldRepository.findAll(pageable);

        log.debug("Found {} metadata fields", metadataFields.getNumberOfElements());
        return metadataFields.stream().toList();
    }

    public String addCategory(String categoryName, String parentId) throws BadRequestException {
        log.info("Attempting to add category: '{}' with parentId: {}", categoryName, parentId);

        if(!categoryRepository.existsById(parentId)){
            throw new ResourceNotFoundException(messageSource.getMessage("error.category.not.found", null, getCurrentLocale()));

        }
        categoryName = categoryName.trim();
        validateRootCategoriesNameUniqueness(categoryName);

        Category newCategory = null;
        if (parentId != null) {
            log.debug("Adding as subcategory under parent ID: {}", parentId);
            Optional<Category> parentCategory = categoryRepository.findById(parentId);

            validateParentAndSiblingCategoriesNameUniqueness(categoryName, parentCategory.get());

            if (productRepository.existsByCategory(parentCategory.get())) {
                log.warn("Cannot add category: parent category has products associated with it");
                throw new BadRequestException(messageSource.getMessage("error.product.exists.for.parent.category", null, getCurrentLocale()));
            }

            newCategory = Category.builder()
                    .parentCategory(parentCategory.get())
                    .name(categoryName)
                    .build();

            Set<Category> subCategoriesUnderParentCategory = parentCategory.get().getSubCategories();
            subCategoriesUnderParentCategory.add(newCategory);
            parentCategory.get().setSubCategories(subCategoriesUnderParentCategory);

        } else {
            log.debug("Adding as root category");
            newCategory = Category.builder()
                    .parentCategory(null)
                    .name(categoryName)
                    .build();
        }
        categoryRepository.save(newCategory);
        log.info("Successfully created category with ID: {}", newCategory.getCategoryId());
        return messageSource.getMessage("success.category.created", new Object[]{newCategory.getCategoryId()}, getCurrentLocale());
    }

    private void validateRootCategoriesNameUniqueness(String categoryName) {
        log.debug("Validating root category name uniqueness for: {}", categoryName);
        if (categoryRepository.findAll().stream()
                .filter(category -> category.getParentCategory() == null)
                .anyMatch(category -> category.getName().trim().equalsIgnoreCase(categoryName))) {
            log.warn("Category with name '{}' already exists at root level", categoryName);
            throw new DuplicateEntryExistsException(messageSource.getMessage("error.category.already.exists", null, getCurrentLocale()));
        }
    }

    private void validateParentAndSiblingCategoriesNameUniqueness(String categoryName, Category parentCategory) {
        log.debug("Validating parent and sibling category name uniqueness for: {}", categoryName);
        Category currentParentCategory = parentCategory;
        while (currentParentCategory.getParentCategory() != null) {
            if (currentParentCategory.getName().trim().equalsIgnoreCase(categoryName)) {
                log.warn("Category with name '{}' already exists in parent hierarchy", categoryName);
                throw new DuplicateEntryExistsException(messageSource.getMessage("error.category.already.exists", null, getCurrentLocale()));
            }
            currentParentCategory = currentParentCategory.getParentCategory();
        }
        for (Category category : parentCategory.getSubCategories()) {
            if (category.getName().equalsIgnoreCase(categoryName)) {
                log.warn("Category with name '{}' already exists as sibling", categoryName);
                throw new DuplicateEntryExistsException(messageSource.getMessage("error.category.already.exists", null, getCurrentLocale()));
            }
        }
        log.debug("Category name '{}' validation passed", categoryName);
    }

    public CategoryDetailsDTO viewCategory(String categoryId) throws BadRequestException {
        log.info("Retrieving details for category with ID: {}", categoryId);
        Category category = validateAndGetCategory(categoryId);

        List<CategoryResponseDTO> parentCategoriesDetails = getParentCategoriesDetails(category);
        List<CategoryResponseDTO> immediateChildrenDetails = getChildrenCategoriesDetails(category);
        HashMap<String, String> metadataFieldsWithValues = getMetadataFieldsWithValues(category);

        log.debug("Found category '{}' with {} parent categories and {} immediate children",
                category.getName(), parentCategoriesDetails.size(), immediateChildrenDetails.size());

        return CategoryDetailsDTO.builder()
                .id(categoryId)
                .name(category.getName())
                .parentCategories(parentCategoriesDetails)
                .immediateChildrens(immediateChildrenDetails)
                .metadataFieldsWithValues(metadataFieldsWithValues)
                .build();
    }

    private Category validateAndGetCategory(String categoryId) throws ResourceNotFoundException {
        log.debug("Validating category existence for ID: {}", categoryId);
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("Category with ID '{}' not found", categoryId);
                    return new ResourceNotFoundException(messageSource.getMessage("error.category.not.found", null, getCurrentLocale()));
                });
    }

    private List<CategoryResponseDTO> getParentCategoriesDetails(Category category) {
        log.debug("Retrieving parent categories for category: {}", category.getName());
        List<Category> parentCategories = new ArrayList<>();
        Category currentParent = category.getParentCategory();

        while (currentParent != null) {
            parentCategories.add(currentParent);
            currentParent = currentParent.getParentCategory();
        }

        log.debug("Found {} parent categories", parentCategories.size());
        return parentCategories.stream()
                .map(this::buildCategoryResponseDTO)
                .toList();
    }

    private List<CategoryResponseDTO> getChildrenCategoriesDetails(Category category) {
        log.debug("Retrieving children categories for category: {}", category.getName());
        List<CategoryResponseDTO> children = category.getSubCategories().stream()
                .map(this::buildCategoryResponseDTO)
                .toList();
        log.debug("Found {} children categories", children.size());
        return children;
    }

    private CategoryResponseDTO buildCategoryResponseDTO(Category category) {
        log.debug("Building CategoryResponseDTO for category ID: {}", category.getCategoryId());
        return CategoryResponseDTO.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getName())
                .parentCategoryId(category.getParentCategory() != null ?
                        category.getParentCategory().getCategoryId() : null)
                .build();
    }

    private HashMap<String, String> getMetadataFieldsWithValues(Category category) {
        log.debug("Retrieving metadata fields for category ID: {}", category.getCategoryId());
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

    public List<CategoryDetailsDTO> viewAllCategories(int max, int offset, String sortBy, String order, String query) {
        log.debug("Viewing categories with max: {}, offset: {}, sortBy: {}, order: {}, query: {}", max, offset, sortBy, order, query);
        Sort sort = Sort.by(Sort.Order.by(sortBy).ignoreCase().with(Sort.Direction.valueOf(order.toUpperCase())));
        Pageable pageable = PageRequest.of(offset, max, sort);

        Page<Category> categories = (query != null && !query.isEmpty()) ?
                categoryRepository.findByNameContainingIgnoreCase(query, pageable) :
                categoryRepository.findAll(pageable);

        log.debug("Found {} categories matching the criteria", categories.getTotalElements());
        return categories.getContent().stream()
                .map(category -> {
                    try {
                        return viewCategory(category.getCategoryId());
                    } catch (BadRequestException e) {
                        log.warn("Failed to view category {}: {}", category.getCategoryId(), e.getMessage());
                        return null;
                    }

                })
                .filter(Objects::nonNull)
                .toList();
    }

    public String updateCategory(String categoryId, String categoryName) throws ResourceNotFoundException {
        log.info("Updating category {} with new name: {}", categoryId, categoryName);
        categoryName = categoryName.trim();
        Category category = validateAndGetCategory(categoryId);

        validateRootCategoriesNameUniqueness(categoryName);

        Category parentCategory = category.getParentCategory();
        validateParentAndSiblingCategoriesNameUniqueness(categoryName, parentCategory);

        Queue<Category> queue = new LinkedList<>(category.getSubCategories());
        while (!queue.isEmpty()) {
            Category child = queue.poll();
            if (child.getName().equalsIgnoreCase(categoryName)) {
                log.warn("Duplicate category name found: {}", categoryName);
                throw new DuplicateEntryExistsException(messageSource.getMessage(
                        "error.category.already.exists", null, getCurrentLocale()));
            }
            queue.addAll(child.getSubCategories());
        }

        category.setName(categoryName);
        log.info("Category {} successfully updated to {}", categoryId, categoryName);
        return messageSource.getMessage("success.category.updated", null, getCurrentLocale());
    }

    public String addMetadataFieldToCategory(CategoryMetadataFieldValueDTO dto) throws BadRequestException , ResourceNotFoundException{
        log.info("Adding metadata field to category: {}", dto.getCategoryId());
        Category category = validateAndGetCategory(dto.getCategoryId());
        HashMap<String, String> mapFieldWithValues = dto.getMetadataFieldWithValues();

        for (Map.Entry<String, String> entry : mapFieldWithValues.entrySet()) {
            String fieldId = entry.getKey();
            String values = entry.getValue();
            log.debug("Processing metadata field: {} with values: {}", fieldId, values);

            CategoryMetadataField metadataField = validateAndGetMetadataField(fieldId);
            validateUniqueValues(values);

            CategoryMetadataFieldValueId categoryMetadataFieldValueId = new CategoryMetadataFieldValueId(
                    category.getCategoryId(),
                    fieldId
            );
            CategoryMetadataFieldValues existingFieldValue = categoryMetadataFieldValuesRepository
                    .findById(categoryMetadataFieldValueId)
                    .orElse(null);

            if (existingFieldValue != null) {
                log.warn("Metadata field already exists for category: {}, field: {}", dto.getCategoryId(), fieldId);
                throw new DuplicateEntryExistsException(messageSource.getMessage(
                        "error.metadata.field.already.exists", new Object[]{dto.getCategoryId()}, getCurrentLocale()));
            }

            CategoryMetadataFieldValues fieldValues = CategoryMetadataFieldValues.builder()
                    .id(categoryMetadataFieldValueId)
                    .category(category)
                    .categoryMetadataField(metadataField)
                    .values(values)
                    .build();

            categoryMetadataFieldValuesRepository.save(fieldValues);
            log.debug("Saved metadata field value for category: {}, field: {}", dto.getCategoryId(), fieldId);
        }

        log.info("Successfully added metadata fields to category: {}", dto.getCategoryId());
        return messageSource.getMessage("success.metadata.field.added", null, getCurrentLocale());
    }

    private CategoryMetadataField validateAndGetMetadataField(String fieldId) {
        Optional<CategoryMetadataField> field = categoryMetadataFieldRepository.findById(fieldId);
        if (field.isEmpty()) {
            log.warn("Metadata field not found: {}", fieldId);
            throw new ResourceNotFoundException(messageSource
                    .getMessage("error.metadata.field.doesnt.exist", null, getCurrentLocale()));
        }
        return field.get();
    }

    private void validateUniqueValues(String values) throws BadRequestException {
        if (values == null || values.trim().isEmpty()) {
            log.warn("Empty metadata values provided");
            throw new BadRequestException(messageSource.getMessage(
                    "error.metadata.values.required", null, getCurrentLocale()));
        }

        Set<String> uniqueValues = Arrays.stream(values.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        if (uniqueValues.size() != values.split(",").length) {
            log.warn("Duplicate metadata values found in: {}", values);
            throw new BadRequestException(messageSource.getMessage(
                    "error.metadata.values.duplicate", null, getCurrentLocale()));
        }
    }

    public String updateMetadataFieldToCategory(CategoryMetadataFieldValueDTO dto) throws BadRequestException {
        log.info("Updating metadata field for category: {}", dto.getCategoryId());
        Category category = validateAndGetCategory(dto.getCategoryId());
        HashMap<String, String> mapFieldWithValues = dto.getMetadataFieldWithValues();
        for (Map.Entry<String, String> entry : mapFieldWithValues.entrySet()) {
            String fieldId = entry.getKey();
            String values = entry.getValue();
            log.debug("Processing update for field: {} with values: {}", fieldId, values);

            validateUniqueValues(values);
            CategoryMetadataFieldValues existingFieldValues = validatedAndAssociatedToCategory(dto.getCategoryId(), fieldId);

            validateUniqueValuesAgainstExisting(values, existingFieldValues.getValues());

            String updatedValues = existingFieldValues.getValues() + "," + values;
            existingFieldValues.setValues(updatedValues);

            categoryMetadataFieldValuesRepository.save(existingFieldValues);
            log.debug("Updated metadata field values for category: {}, field: {}", dto.getCategoryId(), fieldId);
        }

        log.info("Successfully updated metadata fields for category: {}", dto.getCategoryId());
        return messageSource.getMessage("success.metadata.field.updated", null, getCurrentLocale());

    }

    private CategoryMetadataFieldValues validatedAndAssociatedToCategory(String categoryId, String fieldId) throws BadRequestException {
        log.debug("Validating field: {} is associated with category: {}", fieldId, categoryId);
        validateAndGetMetadataField(fieldId);
        CategoryMetadataFieldValueId fieldValueId = new CategoryMetadataFieldValueId(categoryId, fieldId);

        Optional<CategoryMetadataFieldValues> fieldValues = categoryMetadataFieldValuesRepository.findById(fieldValueId);
        if (fieldValues.isEmpty()) {
            log.warn("Field {} not associated with category {}", fieldId, categoryId);
            throw new BadRequestException(messageSource.getMessage(
                    "error.metadata.field.not.associated", new Object[]{fieldId}, getCurrentLocale()));
        }
        return fieldValues.get();
    }

    private void validateUniqueValuesAgainstExisting(String newValues, String existingValues) throws BadRequestException {
        Set<String> existingSet = Arrays.stream(existingValues.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        Set<String> newSet = Arrays.stream(newValues.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        Set<String> intersection = new HashSet<>(existingSet);
        intersection.retainAll(newSet);

        if (!intersection.isEmpty()) {
            log.warn("Duplicate values found between existing and new values: {}", intersection);
            throw new BadRequestException(messageSource.getMessage(
                    "error.metadata.values.already.exists",
                    new Object[]{String.join(", ", intersection)},
                    getCurrentLocale()));
        }
    }

    public List<CategoryDTO> viewAllCategories(String categoryId){
        log.debug("Viewing categories for parent ID: {}", categoryId != null ? categoryId : "root");
        if(categoryId == null || categoryId.isBlank()) {
            List<CategoryDTO> rootCategories = categoryRepository.findByParentCategoryIsNull()
                    .stream()
                    .map(category -> CategoryDTO.builder()
                            .name(category.getName())
                            .id(category.getCategoryId())
                            .build())
                    .toList();
            log.debug("Found {} root categories", rootCategories.size());
            return rootCategories;
        }
        if (!categoryRepository.existsById(categoryId)) {
            log.warn("Category not found: {}", categoryId);
            throw new ResourceNotFoundException(messageSource.getMessage("error.category.doesnt.exist", null, getCurrentLocale()));
        }

        Set<Category> childCategories = categoryRepository.findById(categoryId).get().getSubCategories();
        List<CategoryDTO> categories = childCategories.stream()
                .map(category -> CategoryDTO.builder()
                        .id(category.getCategoryId())
                        .name(category.getName())
                        .build())
                .toList();
        log.debug("Found {} child categories for parent: {}", categories.size(), categoryId);
        return categories;
    }

    public FilteringDetailsDTO getFilteringDetails(String categoryId) throws ResourceNotFoundException {
        log.info("Getting filtering details for category: {}", categoryId);
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
        if (categoryOpt.isEmpty()) {
            log.warn("Category not found: {}", categoryId);
            throw new ResourceNotFoundException(messageSource
                    .getMessage("error.category.doesnt.exist", null, getCurrentLocale()));
        }
        Category category = categoryOpt.get();

        HashMap<String, String> metadataFieldsWithValues = getMetadataFieldsWithValues(category);

        Set<Category> allCategories = getAllSubCategories(category);
        log.debug("Found {} categories including subcategories for {}", allCategories.size(), categoryId);

        List<Product> products = productRepository.findByCategoryIn(allCategories);
        log.debug("Found {} products for category and subcategories", products.size());

        List<String> brands = products.stream()
                .map(Product::getBrand)
                .distinct()
                .toList();

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

        log.debug("Filtering details: brands: {}, price range: {} to {}", brands.size(), minPrice, maxPrice);
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

    public List<CategoryDetailsForSellerDTO> viewCategory() {
        log.debug("Retrieving leaf categories for sellers");
        List<CategoryDetailsForSellerDTO> leafCategories = categoryRepository.findAll().stream()
                .filter(category -> category.getSubCategories() == null || category.getSubCategories().isEmpty())
                .map(category -> CategoryDetailsForSellerDTO.builder()
                        .id(category.getCategoryId())
                        .name(category.getName())
                        .parentCategories(getParentCategoriesDetails(category))
                        .metadataFieldsWithValues(getMetadataFieldsWithValues(category))
                        .build())
                .toList();
        log.debug("Found {} leaf categories for sellers", leafCategories.size());
        return leafCategories;
    }

}
