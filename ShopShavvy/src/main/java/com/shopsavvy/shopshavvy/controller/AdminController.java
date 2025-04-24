package com.shopsavvy.shopshavvy.controller;


import com.shopsavvy.shopshavvy.dto.EmailDTO;
import com.shopsavvy.shopshavvy.dto.categoryDto.CategoryDetailsDTO;
import com.shopsavvy.shopshavvy.dto.categoryDto.CategoryMetadataFieldValueDTO;
import com.shopsavvy.shopshavvy.dto.customerDto.CustomerResponseDTO;
import com.shopsavvy.shopshavvy.dto.productDto.ProductDTO;
import com.shopsavvy.shopshavvy.dto.sellerDto.SellerResponseDTO;
import com.shopsavvy.shopshavvy.model.categories.CategoryMetadataField;
import com.shopsavvy.shopshavvy.service.AdminService;
import com.shopsavvy.shopshavvy.service.AuthenticationService;
import com.shopsavvy.shopshavvy.utilities.SuccessMessageResponse;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shop-shavvy/admin")
public class AdminController {

    private final AuthenticationService authenticationService;
    private final AdminService adminService;

    @PostMapping("/logout")
    public ResponseEntity<SuccessMessageResponse<String>> logout(
            @RequestParam String accessToken,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws MessagingException {
        String message = authenticationService.userLogout(accessToken, httpServletRequest, httpServletResponse);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @PutMapping("/unlock/user")
    public ResponseEntity<SuccessMessageResponse<String>> unlockUser(@Valid @RequestBody EmailDTO emailDTO) {
        String message = adminService.unlockUser(emailDTO);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @GetMapping("/registered-customers")
    public ResponseEntity<SuccessMessageResponse<List<CustomerResponseDTO>>> getAllCustomers(
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "0") int pageOffset,
            @RequestParam(defaultValue = "dateCreated") String sort,
            @RequestParam(required = false) String email) {
        List<CustomerResponseDTO> customers = adminService.getAllCustomers(pageSize, pageOffset, sort, email);
        return ResponseEntity.ok(SuccessMessageResponse.success(customers));
    }

    @GetMapping("/registered-sellers")
    public ResponseEntity<SuccessMessageResponse<List<SellerResponseDTO>>> getAllSellers(
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "0") int pageOffset,
            @RequestParam(defaultValue = "dateCreated") String sort,
            @RequestParam(required = false) String email) {
        List<SellerResponseDTO> sellers = adminService.getAllSellers(pageSize, pageOffset, sort, email);
        return ResponseEntity.ok(SuccessMessageResponse.success(sellers));
    }

    @PutMapping("/activate-customer")
    public ResponseEntity<SuccessMessageResponse<String>> activateCustomer(@RequestParam String customerID) {
        String message = adminService.activateCustomer(customerID);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @PutMapping("/activate-seller")
    public ResponseEntity<SuccessMessageResponse<String>> activateSeller(@RequestParam String sellerID) {
        String message = adminService.activateSeller(sellerID);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @PutMapping("/deactivate-customer")
    public ResponseEntity<SuccessMessageResponse<String>> deactivateCustomer(@RequestParam String customerID) {
        String message = adminService.deactivateCustomer(customerID);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @PutMapping("/deactivate-seller")
    public ResponseEntity<SuccessMessageResponse<String>> deactivateSeller(@RequestParam String sellerID) {
        String message = adminService.deactivateSeller(sellerID);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @PostMapping("/metadata-fields")
    public ResponseEntity<SuccessMessageResponse<String>> addField(@RequestParam String fieldName){
        String message = adminService.addMetadataField(fieldName);
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessMessageResponse.success(message));
    }

    @GetMapping("/metadata-fields")
    public ResponseEntity<SuccessMessageResponse<List<CategoryMetadataField>>> getAllMetadataFields(
            @RequestParam(required = false, defaultValue = "10") int max,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false, defaultValue = "name") String sort,
            @RequestParam(required = false, defaultValue = "ASC") String order,
            @RequestParam(required = false) String query){
        List<CategoryMetadataField> metadataFields = adminService.getAllFields(max, offset, sort, order, query);
        return ResponseEntity.ok(SuccessMessageResponse.success(metadataFields));

    }

    @PostMapping("/category")
    public ResponseEntity<SuccessMessageResponse<String>> addNewCategory(
            @RequestParam String categoryName,
            @RequestParam(required = false) String parentCategoryId) throws BadRequestException {
        String message = adminService.addCategory(categoryName, parentCategoryId);
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessMessageResponse.success(message));
    }

    @GetMapping("/category")
    public ResponseEntity<CategoryDetailsDTO> viewCategory(@RequestParam String categoryId) throws BadRequestException {
        CategoryDetailsDTO categeoryDetailsDTO = adminService.viewCategory(categoryId);
        return ResponseEntity.ok(categeoryDetailsDTO);

    }

    @GetMapping("/all/categories")
    public ResponseEntity<List<CategoryDetailsDTO>> viewAllCategories(
            @RequestParam(required = false, defaultValue = "10") int max,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false, defaultValue = "name") String sort,
            @RequestParam(required = false, defaultValue = "ASC") String order,
            @RequestParam(required = false) String query
    ) {
        List<CategoryDetailsDTO> categeoryDetailsDTOS = adminService.viewAllCategories(max, offset, sort, order, query);
        return ResponseEntity.ok(categeoryDetailsDTOS);
    }

    @PutMapping("/category")
    public ResponseEntity<SuccessMessageResponse<String>> updateCategory(@RequestParam(required = true) String categoryName,
                                                 @RequestParam(required = true) String categoryId) throws BadRequestException {
        String message = adminService.updateCategory(categoryId, categoryName);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @PostMapping("/category/metadata-field")
    public ResponseEntity<SuccessMessageResponse<String>> addMetadataFieldValues(
            @RequestBody CategoryMetadataFieldValueDTO dto) throws BadRequestException {
        String message = adminService.addMetadataFieldToCategory(dto);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @PutMapping("/category/metadata-field")
    public ResponseEntity<SuccessMessageResponse<String>> updateMetadataFieldValues(
            @RequestBody CategoryMetadataFieldValueDTO dto) throws BadRequestException{
        String message = adminService.updateMetadataFieldToCategory(dto);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<SuccessMessageResponse<ProductDTO>> viewProduct(
            @PathVariable String id) throws BadRequestException {
        return ResponseEntity.ok(SuccessMessageResponse.success(
                adminService.viewProduct(id)));
    }

    @PutMapping("/products/{productId}/deactivate")
    public ResponseEntity<SuccessMessageResponse<String>> deactivateProduct(
            @PathVariable String productId) throws BadRequestException {
        return ResponseEntity.ok(SuccessMessageResponse.success(
                adminService.deactivateProduct(productId)));
    }

    @PutMapping("/products/{productId}/activate")
    public ResponseEntity<SuccessMessageResponse<String>> activateProduct(
            @PathVariable String productId) throws BadRequestException {
        return ResponseEntity.ok(SuccessMessageResponse.success(
                adminService.activateProduct(productId)));
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductDTO>> viewAllProducts(
            @RequestParam(required = false, defaultValue = "name") String sort,
            @RequestParam(required = false, defaultValue = "asc") String order,
            @RequestParam(required = false, defaultValue = "10") int max,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false) Map<String, String> filter) {

        List<ProductDTO> products = adminService.viewAllProducts(sort, order, max, offset, filter);
        return ResponseEntity.ok(products);
    }

}
