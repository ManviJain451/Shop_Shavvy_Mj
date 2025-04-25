package com.shopsavvy.shopshavvy.controller;


import com.shopsavvy.shopshavvy.configuration.UserDetailsImpl;
import com.shopsavvy.shopshavvy.dto.EmailDTO;
import com.shopsavvy.shopshavvy.dto.categoryDto.CategoryDetailsDTO;
import com.shopsavvy.shopshavvy.dto.categoryDto.CategoryMetadataFieldValueDTO;
import com.shopsavvy.shopshavvy.dto.customerDto.CustomerResponseDTO;
import com.shopsavvy.shopshavvy.dto.productDto.ProductDTO;
import com.shopsavvy.shopshavvy.dto.sellerDto.SellerResponseDTO;
import com.shopsavvy.shopshavvy.dto.userDto.UserProfileDTO;
import com.shopsavvy.shopshavvy.model.categories.CategoryMetadataField;
import com.shopsavvy.shopshavvy.service.AdminService;
import com.shopsavvy.shopshavvy.service.AuthenticationService;
import com.shopsavvy.shopshavvy.service.CategoryService;
import com.shopsavvy.shopshavvy.service.ProductService;
import com.shopsavvy.shopshavvy.utilities.SuccessMessageResponse;
import com.shopsavvy.shopshavvy.validation.groups.OnUpdate;
import jakarta.mail.MessagingException;
import jakarta.mail.SendFailedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AuthenticationService authenticationService;
    private final AdminService adminService;
    private final CategoryService categoryService;
    private final ProductService productService;

    @GetMapping("/profile")
    public ResponseEntity<SuccessMessageResponse<UserProfileDTO>> viewProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        UserProfileDTO dto = adminService.getProfile(userDetails);
        return ResponseEntity.ok(SuccessMessageResponse.success(dto));
    }

    @PutMapping("/profile")
    public ResponseEntity<SuccessMessageResponse<String>> updateProfile(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                                                                        @Validated(OnUpdate.class) @ModelAttribute UserProfileDTO userProfileDTO) throws IOException {

        String message = adminService.updateProfile(userDetailsImpl, userProfileDTO);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @PostMapping("/logout")
    public ResponseEntity<SuccessMessageResponse<String>> logout(
            @RequestParam String accessToken,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws MessagingException {
        String message = authenticationService.userLogout(accessToken, httpServletRequest, httpServletResponse);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @PutMapping("/users/unlock")
    public ResponseEntity<SuccessMessageResponse<String>> unlockUser(@Valid @RequestBody EmailDTO emailDTO) {
        String message = adminService.unlockUser(emailDTO);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @GetMapping("/customers")
    public ResponseEntity<SuccessMessageResponse<List<CustomerResponseDTO>>> getAllCustomers(
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "0") int pageOffset,
            @RequestParam(required = false, defaultValue = "dateCreated") String sort,
            @RequestParam(required = false) String email) {
        List<CustomerResponseDTO> customers = adminService.getAllCustomers(pageSize, pageOffset, sort, email);
        return ResponseEntity.ok(SuccessMessageResponse.success(customers));
    }

    @GetMapping("/sellers")
    public ResponseEntity<SuccessMessageResponse<List<SellerResponseDTO>>> getAllSellers(
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "0") int pageOffset,
            @RequestParam(required = false, defaultValue = "dateCreated") String sort,
            @RequestParam(required = false) String email) {
        List<SellerResponseDTO> sellers = adminService.getAllSellers(pageSize, pageOffset, sort, email);
        return ResponseEntity.ok(SuccessMessageResponse.success(sellers));
    }

    @PutMapping("/customers/{customerId}/activate")
    public ResponseEntity<SuccessMessageResponse<String>> activateCustomer(@PathVariable String customerId) {
        String message = adminService.activateCustomer(customerId);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @PutMapping("/sellers/{sellerId}/activate")
    public ResponseEntity<SuccessMessageResponse<String>> activateSeller(@PathVariable String sellerId) {
        String message = adminService.activateSeller(sellerId);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @PutMapping("/customers/{customerId}/deactivate")
    public ResponseEntity<SuccessMessageResponse<String>> deactivateCustomer(@PathVariable String customerId) {
        String message = adminService.deactivateCustomer(customerId);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @PutMapping("/sellers/{sellerId}/deactivate")
    public ResponseEntity<SuccessMessageResponse<String>> deactivateSeller(@PathVariable String sellerId) {
        String message = adminService.deactivateSeller(sellerId);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @PostMapping("/metadata-fields")
    public ResponseEntity<SuccessMessageResponse<String>> addField(@RequestParam String fieldName){
        String message = categoryService.addMetadataField(fieldName);
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessMessageResponse.success(message));
    }

    @GetMapping("/metadata-fields")
    public ResponseEntity<SuccessMessageResponse<List<CategoryMetadataField>>> getAllMetadataFields(
            @RequestParam(required = false, defaultValue = "10") int max,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false, defaultValue = "name") String sort,
            @RequestParam(required = false, defaultValue = "ASC") String order,
            @RequestParam(required = false) String query){
        List<CategoryMetadataField> metadataFields = categoryService.getAllMetadataFields(max, offset, sort, order, query);
        return ResponseEntity.ok(SuccessMessageResponse.success(metadataFields));

    }

    @PostMapping("/categories")
    public ResponseEntity<SuccessMessageResponse<String>> addNewCategory(
            @RequestParam String categoryName,
            @RequestParam(required = false) String parentCategoryId) throws BadRequestException {
        String message = categoryService.addCategory(categoryName, parentCategoryId);
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessMessageResponse.success(message));
    }

    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<SuccessMessageResponse<CategoryDetailsDTO>> viewCategory(@PathVariable String categoryId) throws BadRequestException {
        CategoryDetailsDTO categeoryDetailsDTO = categoryService.viewCategory(categoryId);
        return ResponseEntity.ok(SuccessMessageResponse.success(categeoryDetailsDTO));

    }

    @GetMapping("/categories")
    public ResponseEntity<SuccessMessageResponse<List<CategoryDetailsDTO>>> viewAllCategories(
            @RequestParam(required = false, defaultValue = "10") int max,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false, defaultValue = "name") String sort,
            @RequestParam(required = false, defaultValue = "ASC") String order,
            @RequestParam(required = false) String query
    ) {
        List<CategoryDetailsDTO> categeoryDetailsDTOS = categoryService.viewAllCategories(max, offset, sort, order, query);
        return ResponseEntity.ok(SuccessMessageResponse.success(categeoryDetailsDTOS));
    }

    @PutMapping("/categories/{categoryId}")
    public ResponseEntity<SuccessMessageResponse<String>> updateCategory(@RequestParam String categoryName,
                                                 @PathVariable String categoryId) throws BadRequestException {
        String message = categoryService.updateCategory(categoryId, categoryName);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @PostMapping("/categories/metadata-fields")
    public ResponseEntity<SuccessMessageResponse<String>> addMetadataFieldValues(
            @Valid @RequestBody CategoryMetadataFieldValueDTO dto) throws BadRequestException {
        String message = categoryService.addMetadataFieldToCategory(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessMessageResponse.success(message));
    }

    @PutMapping("/categories/metadata-fields")
    public ResponseEntity<SuccessMessageResponse<String>> updateMetadataFieldValues(
            @Valid @RequestBody CategoryMetadataFieldValueDTO dto) throws BadRequestException{
        String message = categoryService.updateMetadataFieldToCategory(dto);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<SuccessMessageResponse<ProductDTO>> viewProduct(@PathVariable String id) throws BadRequestException {
        return ResponseEntity.ok(SuccessMessageResponse.success(productService.viewProduct(id)));
    }

    @PutMapping("/products/{productId}/toggle-status")
    public ResponseEntity<SuccessMessageResponse<String>> toggleStatus(
            @PathVariable String productId) throws BadRequestException, SendFailedException {
        return ResponseEntity.ok(SuccessMessageResponse.success(
                productService.toggleProductStatus(productId)));
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductDTO>> viewAllProducts(
            @RequestParam(required = false, defaultValue = "name") String sort,
            @RequestParam(required = false, defaultValue = "asc") String order,
            @RequestParam(required = false, defaultValue = "10") int max,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false) Map<String, String> filter) {

        List<ProductDTO> products = productService.viewAllProducts(sort, order, max, offset, filter);
        return ResponseEntity.ok(products);
    }

}
