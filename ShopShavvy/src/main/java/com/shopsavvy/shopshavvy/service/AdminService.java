package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.dto.EmailDTO;
import com.shopsavvy.shopshavvy.dto.categoryDto.CategoryMetadataFieldValueDTO;
import com.shopsavvy.shopshavvy.dto.categoryDto.CategoryResponseDTO;
import com.shopsavvy.shopshavvy.dto.categoryDto.CategeoryDetailsDTO;
import com.shopsavvy.shopshavvy.dto.categoryDto.MetadataFieldDTO;
import com.shopsavvy.shopshavvy.dto.customerDto.CustomerResponseDTO;
import com.shopsavvy.shopshavvy.dto.sellerDto.SellerResponseDTO;
import com.shopsavvy.shopshavvy.exception.AlreadyActivatedException;
import com.shopsavvy.shopshavvy.exception.DuplicateEntryExistsException;
import com.shopsavvy.shopshavvy.exception.UserNotFoundException;
import com.shopsavvy.shopshavvy.model.categories.Category;
import com.shopsavvy.shopshavvy.model.categories.CategoryMetadataField;
import com.shopsavvy.shopshavvy.model.categories.CategoryMetadataFieldValueId;
import com.shopsavvy.shopshavvy.model.categories.CategoryMetadataFieldValues;
import com.shopsavvy.shopshavvy.model.users.Customer;
import com.shopsavvy.shopshavvy.model.users.Seller;
import com.shopsavvy.shopshavvy.model.users.User;
import com.shopsavvy.shopshavvy.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final CustomerRepository customerRepository;
    private final SellerRepository sellerRepository;
    private final MessageSource messageSource;
    private final CategoryMetadataFieldRepository categoryMetadataFieldRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMetadataFieldValuesRepository categoryMetadataFieldValuesRepository;

    private Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    public String unlockUser(EmailDTO emailDTO) {
        User user = userRepository.findByEmail(emailDTO.getEmail())
                .orElseThrow(() -> new UserNotFoundException(
                        messageSource.getMessage("user.not.found.with.email", new Object[]{emailDTO.getEmail()}, getCurrentLocale())));

        if (!user.isLocked()) {
            return messageSource.getMessage("user.already.unlocked", null, getCurrentLocale());
        }

        user.setLocked(false);
        user.setInvalidAttemptCount(0);
        userRepository.save(user);
        return messageSource.getMessage("user.unlocked.success", null, getCurrentLocale());
    }

    public List<CustomerResponseDTO> getAllCustomers(int pageSize, int pageOffset, String sort, String email) {
        Pageable pageable = PageRequest.of(pageOffset, pageSize, Sort.by(sort));
        Page<Customer> customers = (email != null && !email.isEmpty()) ?
                customerRepository.findByEmailContainingIgnoreCase(email, pageable) :
                customerRepository.findAll(pageable);

        return customers.stream().map(customer -> new CustomerResponseDTO(
                customer.getId(),
                customer.getFirstName() + " " +
                        (customer.getMiddleName() != null ? customer.getMiddleName() + " " : "") +
                        customer.getLastName(),
                customer.getEmail(),
                customer.getIsActive()
        )).collect(Collectors.toList());
    }

    public List<SellerResponseDTO> getAllSellers(int pageSize, int pageOffset, String sort, String email) {
        Pageable pageable = PageRequest.of(pageOffset, pageSize, Sort.by(sort));
        Page<Seller> sellers = (email != null && !email.isEmpty()) ?
                sellerRepository.findByEmailContainingIgnoreCase(email, pageable) :
                sellerRepository.findAll(pageable);

        return sellers.stream().map(seller -> new SellerResponseDTO(
                seller.getId(),
                seller.getFirstName() + " " +
                        (seller.getMiddleName() != null ? seller.getMiddleName() + " " : "") +
                        seller.getLastName(),
                seller.getEmail(),
                seller.getIsActive(),
                seller.getCompanyName(),
                seller.getAddresses(),
                seller.getCompanyContact()
        )).collect(Collectors.toList());
    }

    public String activateCustomer(String customerID) {
        Customer customer = customerRepository.findById(customerID)
                .orElseThrow(() -> new UserNotFoundException(
                        messageSource.getMessage("customer.not.found", new Object[]{customerID}, getCurrentLocale())));

        if (customer.getIsActive()) {
            throw new AlreadyActivatedException(messageSource.getMessage("customer.already.activated", null, getCurrentLocale()));
        }

        customer.setIsActive(true);
        customerRepository.save(customer);


        try {
            emailService.sendVerificationEmail(customer.getEmail(), "Account Activated", "Your account has been successfully activated.");
        } catch (Exception e) {
            throw new RuntimeException(messageSource.getMessage("email.failed.activation", null, getCurrentLocale()));
        }

        return messageSource.getMessage("customer.activated.success", null, getCurrentLocale());
    }

    public String activateSeller(String sellerID) {
        Seller seller = sellerRepository.findById(sellerID)
                .orElseThrow(() -> new UserNotFoundException(
                        messageSource.getMessage("seller.not.found", new Object[]{sellerID}, getCurrentLocale())));

        if (seller.getIsActive()) {
            throw new AlreadyActivatedException(messageSource.getMessage("seller.already.activated", null, getCurrentLocale()));
        }

        seller.setIsActive(true);
        sellerRepository.save(seller);

        try {
            emailService.sendVerificationEmail(seller.getEmail(),
                    "Account Activated", "Your account has been successfully activated.");
        } catch (Exception e) {
            throw new RuntimeException(messageSource.getMessage("email.failed.activation", null, getCurrentLocale()));
        }

        return messageSource.getMessage("seller.activated.success", null, getCurrentLocale());
    }

    public String deactivateCustomer(String customerID) {
        Customer customer = customerRepository.findById(customerID)
                .orElseThrow(() -> new UserNotFoundException(
                        messageSource.getMessage("customer.not.found", new Object[]{customerID}, getCurrentLocale())));

        if (!customer.getIsActive()) {
            return messageSource.getMessage("customer.already.deactivated", null, getCurrentLocale());
        }

        customer.setIsActive(false);
        customerRepository.save(customer);

        try {
            emailService.sendVerificationEmail(customer.getEmail(), "Account Deactivated", "Your account has been successfully deactivated.");
        } catch (Exception e) {
            throw new RuntimeException(messageSource.getMessage("email.failed.deactivation", null, getCurrentLocale()));
        }

        return messageSource.getMessage("customer.deactivated.success", null, getCurrentLocale());
    }

    public String deactivateSeller(String sellerID) {
        Seller seller = sellerRepository.findById(sellerID)
                .orElseThrow(() -> new UserNotFoundException(
                        messageSource.getMessage("seller.not.found", new Object[]{sellerID}, getCurrentLocale())));

        if (!seller.getIsActive()) {
            return messageSource.getMessage("seller.already.deactivated", null, getCurrentLocale());
        }

        seller.setIsActive(false);
        sellerRepository.save(seller);

        try {
            emailService.sendVerificationEmail(seller.getEmail(), "Account Deactivated", "Your account has been successfully deactivated.");
        } catch (Exception e) {
            throw new RuntimeException(messageSource.getMessage("email.failed.deactivation", null, getCurrentLocale()));
        }

        return messageSource.getMessage("seller.deactivated.success", null, getCurrentLocale());
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


    public List<CategoryMetadataField> getAllFields(int max, int offset, String sortBy, String order, String query) {
        Sort sort = Sort.by(Sort.Order.by(sortBy).ignoreCase().with(Sort.Direction.valueOf(order.toUpperCase())));
        Pageable pageable = PageRequest.of(offset, max, sort);

        Page<CategoryMetadataField> metadataFields = (query != null && !query.isEmpty()) ?
                categoryMetadataFieldRepository.findByNameContainingIgnoreCase(query, pageable) :
                categoryMetadataFieldRepository.findAll(pageable);

        return metadataFields.stream().toList();
    }

    public String addCategory(String categoryName, String parentId) throws BadRequestException {

        if (categoryRepository.findAll().stream()
                .filter(category -> category.getParentCategory() == null)
                .anyMatch(category -> category.getName().equalsIgnoreCase(categoryName))) {
            throw new DuplicateEntryExistsException(messageSource.getMessage("error.category.already.exists", null, getCurrentLocale()));
        }

        Category newCategory = null;
        if (parentId != null) {
            Optional<Category> parentCategory = categoryRepository.findById(parentId);

            Category currentParentCategory = parentCategory.get();
            while (currentParentCategory.getParentCategory() != null) {
                if (currentParentCategory.getName().equalsIgnoreCase(categoryName)) {
                    throw new DuplicateEntryExistsException(messageSource.getMessage("error.category.already.exists", null, getCurrentLocale()));
                }
                currentParentCategory = currentParentCategory.getParentCategory();
            }


            for (Category category : parentCategory.get().getSubCategories()) {
                if (category.getName().equalsIgnoreCase(categoryName)) {
                    throw new DuplicateEntryExistsException(messageSource.getMessage("error.category.already.exists", null, getCurrentLocale()));
                }
            }

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

    public CategeoryDetailsDTO viewCategory(String categoryId) throws BadRequestException {
        Category category = validateAndGetCategory(categoryId);

        List<CategoryResponseDTO> parentCategoriesDetails = getParentCategoriesDetails(category);
        List<CategoryResponseDTO> immediateChildrenDetails = getChildrenCategoriesDetails(category);
        List<MetadataFieldDTO> metadataFields = getMetadataFieldDTOs(categoryId);

        return CategeoryDetailsDTO.builder()
                .id(categoryId)
                .name(category.getName())
                .parentCategories(parentCategoriesDetails)
                .immediateChildrens(immediateChildrenDetails)
                .metadataFields(metadataFields)
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
                .collect(Collectors.toList());
    }

    private List<CategoryResponseDTO> getChildrenCategoriesDetails(Category category) {
        return category.getSubCategories().stream()
                .map(this::buildCategoryResponseDTO)
                .collect(Collectors.toList());
    }

    private CategoryResponseDTO buildCategoryResponseDTO(Category category) {
        return CategoryResponseDTO.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getName())
                .parentCategoryId(category.getParentCategory() != null ?
                        category.getParentCategory().getCategoryId() : null)
                .metadataFields(getMetadataFieldDTOs(category.getCategoryId()))
                .build();
    }

    private List<MetadataFieldDTO> getMetadataFieldDTOs(String categoryId) {
        List<CategoryMetadataFieldValues> metadataFields = categoryMetadataFieldValuesRepository
                .findMetadataFieldByCategoryId(categoryId);

        return metadataFields.stream()
                .map(field -> MetadataFieldDTO.builder()
                        .id(field.getCategoryMetadataField().getId())
                        .name(field.getCategoryMetadataField().getName())
                        .values(Arrays.asList(field.getValues().split("\\s*,\\s*")))
                        .build())
                .collect(Collectors.toList());
    }

    public List<CategeoryDetailsDTO> viewAllCategories() {
        return categoryRepository.findAll().stream()
                .map(category -> {
                    try {
                        return viewCategory(category.getCategoryId());
                    } catch (BadRequestException e) {
                        return null;
                    }

                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public String updateCategory(String categoryId, String categoryName) throws BadRequestException {
        Category category = validateAndGetCategory(categoryId);

        if (categoryRepository.findAll().stream()
                .filter(cat -> cat.getParentCategory() == null)
                .anyMatch(cat -> cat.getName().equalsIgnoreCase(categoryName))) {
            throw new DuplicateEntryExistsException(messageSource.getMessage("error.category.already.exists", null, getCurrentLocale()));
        }

        Category parentCategory = category.getParentCategory();
        Category currentParentCategory = parentCategory;
        while (currentParentCategory.getParentCategory() != null) {
            if (currentParentCategory.getName().equalsIgnoreCase(categoryName)) {
                throw new DuplicateEntryExistsException(messageSource.getMessage("error.category.already.exists", null, getCurrentLocale()));
            }
            currentParentCategory = currentParentCategory.getParentCategory();
        }


        for (Category subCategory : parentCategory.getSubCategories()) {
            if (subCategory.getName().equalsIgnoreCase(categoryName)) {
                throw new DuplicateEntryExistsException(messageSource.getMessage("error.category.already.exists", null, getCurrentLocale()));
            }
        }

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


}
