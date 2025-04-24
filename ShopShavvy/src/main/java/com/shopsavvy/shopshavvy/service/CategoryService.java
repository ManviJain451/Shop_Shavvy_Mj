package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.dto.categoryDto.*;
import com.shopsavvy.shopshavvy.exception.DuplicateEntryExistsException;
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
        if (categoryMetadataFieldRepository.existsByName(fieldName)) {
            throw new DuplicateEntryExistsException(messageSource.getMessage("error.field.already.exists", null, getCurrentLocale()));
        }
        CategoryMetadataField categoryMetadataField = CategoryMetadataField.builder()
                .name(fieldName).build();
        categoryMetadataFieldRepository.save(categoryMetadataField);
        return messageSource.getMessage("success.created.metadata.field", new Object[]{categoryMetadataField.getId()}, getCurrentLocale());
    }

    public List<CategoryMetadataField> getAllMetadataFields(int max, int offset, String sortBy, String order, String query) {
        Sort sort = Sort.by(Sort.Order.by(sortBy).ignoreCase().with(Sort.Direction.valueOf(order.toUpperCase())));
        Pageable pageable = PageRequest.of(offset, max, sort);

        Page<CategoryMetadataField> metadataFields = (query != null && !query.isEmpty()) ?
                categoryMetadataFieldRepository.findByNameContainingIgnoreCase(query, pageable) :
                categoryMetadataFieldRepository.findAll(pageable);

        return metadataFields.stream().toList();
    }

    public String addCategory(String categoryName, String parentId) throws BadRequestException {

        validateRootCategoriesNameUniqueness(categoryName);

        Category newCategory = null;
        if (parentId != null) {
            Optional<Category> parentCategory = categoryRepository.findById(parentId);

            validateParentAndSiblingCategoriesNameUniqueness(categoryName, parentCategory.get());

            if (productRepository.existsByCategory(parentCategory.get())) {
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
            newCategory = Category.builder()
                    .parentCategory(null)
                    .name(categoryName)
                    .build();
        }
        categoryRepository.save(newCategory);
        return messageSource.getMessage("success.category.created", new Object[]{newCategory.getCategoryId()}, getCurrentLocale());

    }

    private void validateRootCategoriesNameUniqueness(String categoryName) {
        if (categoryRepository.findAll().stream()
                .filter(category -> category.getParentCategory() == null)
                .anyMatch(category -> category.getName().equalsIgnoreCase(categoryName))) {
            throw new DuplicateEntryExistsException(messageSource.getMessage("error.category.already.exists", null, getCurrentLocale()));
        }
    }

    private void validateParentAndSiblingCategoriesNameUniqueness(String categoryName, Category parentCategory) {
        Category currentParentCategory = parentCategory;
        while (currentParentCategory.getParentCategory() != null) {
            if (currentParentCategory.getName().equalsIgnoreCase(categoryName)) {
                throw new DuplicateEntryExistsException(messageSource.getMessage("error.category.already.exists", null, getCurrentLocale()));
            }
            currentParentCategory = currentParentCategory.getParentCategory();
        }
        for (Category category : parentCategory.getSubCategories()) {
            if (category.getName().equalsIgnoreCase(categoryName)) {
                throw new DuplicateEntryExistsException(messageSource.getMessage("error.category.already.exists", null, getCurrentLocale()));
            }
        }
    }

    public CategoryDetailsDTO viewCategory(String categoryId) throws BadRequestException {
        Category category = validateAndGetCategory(categoryId);

        List<CategoryResponseDTO> parentCategoriesDetails = getParentCategoriesDetails(category);
        List<CategoryResponseDTO> immediateChildrenDetails = getChildrenCategoriesDetails(category);
        HashMap<String, String> metadataFieldsWithValues = getMetadataFieldsWithValues(category);

        return CategoryDetailsDTO.builder()
                .id(categoryId)
                .name(category.getName())
                .parentCategories(parentCategoriesDetails)
                .immediateChildrens(immediateChildrenDetails)
                .metadataFieldsWithValues(metadataFieldsWithValues)
                .build();
    }

    private Category validateAndGetCategory(String categoryId) throws BadRequestException {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BadRequestException("Category doesn't exists."));
    }

    private List<CategoryResponseDTO> getParentCategoriesDetails(Category category) {
        List<Category> parentCategories = new ArrayList<>();
        Category currentParent = category.getParentCategory();

        while (currentParent != null) {
            parentCategories.add(currentParent);
            currentParent = currentParent.getParentCategory();
        }

        return parentCategories.stream()
                .map(this::buildCategoryResponseDTO)
                .toList();
    }

    private List<CategoryResponseDTO> getChildrenCategoriesDetails(Category category) {
        return category.getSubCategories().stream()
                .map(this::buildCategoryResponseDTO)
                .toList();
    }

    private CategoryResponseDTO buildCategoryResponseDTO(Category category) {
        return CategoryResponseDTO.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getName())
                .parentCategoryId(category.getParentCategory() != null ?
                        category.getParentCategory().getCategoryId() : null)
                .build();
    }

    private HashMap<String, String> getMetadataFieldsWithValues(Category category) {
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
        Sort sort = Sort.by(Sort.Order.by(sortBy).ignoreCase().with(Sort.Direction.valueOf(order.toUpperCase())));
        Pageable pageable = PageRequest.of(offset, max, sort);

        Page<Category> categories = (query != null && !query.isEmpty()) ?
                categoryRepository.findByNameContainingIgnoreCase(query, pageable) :
                categoryRepository.findAll(pageable);

        return categories.getContent().stream()
                .map(category -> {
                    try {
                        return viewCategory(category.getCategoryId());
                    } catch (BadRequestException e) {
                        return null;
                    }

                })
                .filter(Objects::nonNull)
                .toList();
    }

    public String updateCategory(String categoryId, String categoryName) throws BadRequestException {
        Category category = validateAndGetCategory(categoryId);

        validateRootCategoriesNameUniqueness(categoryName);

        Category parentCategory = category.getParentCategory();
        validateParentAndSiblingCategoriesNameUniqueness(categoryName, parentCategory);

        Queue<Category> queue = new LinkedList<>(category.getSubCategories());
        while (!queue.isEmpty()) {
            Category child = queue.poll();
            if (child.getName().equalsIgnoreCase(categoryName)) {
                throw new DuplicateEntryExistsException(messageSource.getMessage(
                        "error.category.already.exists", null, getCurrentLocale()));
            }
            queue.addAll(child.getSubCategories());
        }

        category.setName(categoryName);
        return messageSource.getMessage("success.category.updated", null, getCurrentLocale());
    }

    public String addMetadataFieldToCategory(CategoryMetadataFieldValueDTO dto) throws BadRequestException {
        Category category = validateAndGetCategory(dto.getCategoryId());
        HashMap<String, String> mapFieldWithValues = dto.getMetadataFieldWithValues();

        for (Map.Entry<String, String> entry : mapFieldWithValues.entrySet()) {
            String fieldId = entry.getKey();
            String values = entry.getValue();

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
        }

        return messageSource.getMessage("success.metadata.field.added", null, getCurrentLocale());
    }

    private CategoryMetadataField validateAndGetMetadataField(String fieldId) throws BadRequestException {
        return categoryMetadataFieldRepository.findById(fieldId)
                .orElseThrow(() -> new BadRequestException(messageSource
                        .getMessage("error.metadata.field.doesnt.exist", null, getCurrentLocale())));
    }

    private void validateUniqueValues(String values) throws BadRequestException {
        if (values == null || values.trim().isEmpty()) {
            throw new BadRequestException(messageSource.getMessage(
                    "error.metadata.values.required", null, getCurrentLocale()));
        }

        Set<String> uniqueValues = Arrays.stream(values.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        if (uniqueValues.size() != values.split(",").length) {
            throw new BadRequestException(messageSource.getMessage(
                    "error.metadata.values.duplicate", null, getCurrentLocale()));
        }
    }

    public String updateMetadataFieldToCategory(CategoryMetadataFieldValueDTO dto) throws BadRequestException {
        Category category = validateAndGetCategory(dto.getCategoryId());
        HashMap<String, String> mapFieldWithValues = dto.getMetadataFieldWithValues();
        for (Map.Entry<String, String> entry : mapFieldWithValues.entrySet()) {
            String fieldId = entry.getKey();
            String values = entry.getValue();

            validateUniqueValues(values);
            CategoryMetadataFieldValues existingFieldValues = validatedAndAssociatedToCategory(dto.getCategoryId(), fieldId);

            validateUniqueValuesAgainstExisting(values, existingFieldValues.getValues());

            String updatedValues = existingFieldValues.getValues() + "," + values;
            existingFieldValues.setValues(updatedValues);

            categoryMetadataFieldValuesRepository.save(existingFieldValues);
        }

        return messageSource.getMessage("success.metadata.field.updated", null, getCurrentLocale());

    }

    private CategoryMetadataFieldValues validatedAndAssociatedToCategory(String categoryId, String fieldId) throws BadRequestException {
        CategoryMetadataField metadataField = validateAndGetMetadataField(fieldId);
        CategoryMetadataFieldValueId fieldValueId = new CategoryMetadataFieldValueId(categoryId, fieldId);
        return categoryMetadataFieldValuesRepository.findById(fieldValueId)
                .orElseThrow(() -> new BadRequestException(messageSource.getMessage(
                        "error.metadata.field.not.associated", new Object[]{fieldId}, getCurrentLocale())));
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
            throw new BadRequestException(messageSource.getMessage(
                    "error.metadata.values.already.exists",
                    new Object[]{String.join(", ", intersection)},
                    getCurrentLocale()));
        }
    }

    public List<CategoryDTO> viewAllCategories(String categoryId) throws BadRequestException {
        if(categoryId == null || categoryId.isBlank()) {
            return categoryRepository.findByParentCategoryIsNull()
                    .stream()
                    .map(category -> CategoryDTO.builder()
                            .name(category.getName())
                            .id(category.getCategoryId())
                            .build())
                    .toList();
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
                .toList();
    }

    public FilteringDetailsDTO getFilteringDetails(String categoryId) throws BadRequestException {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BadRequestException(messageSource
                        .getMessage("error.category.doesnt.exist", null, getCurrentLocale())));

        HashMap<String, String> metadataFieldsWithValues = getMetadataFieldsWithValues(category);

        Set<Category> allCategories = getAllSubCategories(category);
        List<Product> products = productRepository.findByCategoryIn(allCategories);

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
                        .metadataFieldsWithValues(getMetadataFieldsWithValues(category))
                        .build())
                .toList();
    }

}
