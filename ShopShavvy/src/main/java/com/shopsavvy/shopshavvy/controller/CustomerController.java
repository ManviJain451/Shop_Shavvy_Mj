package com.shopsavvy.shopshavvy.controller;

import com.shopsavvy.shopshavvy.dto.address.AddressDTO;
import com.shopsavvy.shopshavvy.dto.address.CustomerAddressDTO;
import com.shopsavvy.shopshavvy.dto.category.CategoryDTO;
import com.shopsavvy.shopshavvy.dto.category.FilteringDetailsDTO;
import com.shopsavvy.shopshavvy.dto.customer.CustomerProfileDTO;
import com.shopsavvy.shopshavvy.dto.product.ProductDTO;
import com.shopsavvy.shopshavvy.configuration.UserDetailsImpl;
import com.shopsavvy.shopshavvy.service.AuthenticationService;
import com.shopsavvy.shopshavvy.service.CategoryService;
import com.shopsavvy.shopshavvy.service.CustomerService;
import com.shopsavvy.shopshavvy.service.ProductService;
import com.shopsavvy.shopshavvy.utilities.SuccessMessageResponse;
import com.shopsavvy.shopshavvy.validation.groups.OnCreate;
import com.shopsavvy.shopshavvy.validation.groups.OnUpdate;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final AuthenticationService authenticationService;
    private final CustomerService customerService;
    private final CategoryService categoryService;
    private final ProductService productService;

    @PostMapping("/logout")
    public ResponseEntity<SuccessMessageResponse<String>> logout(@RequestParam  String accessToken,
                                                                HttpServletRequest httpServletRequest,
                                                                HttpServletResponse httpServletResponse) throws MessagingException {
        String message = authenticationService.userLogout(accessToken, httpServletRequest, httpServletResponse);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @GetMapping("/profile")
    public ResponseEntity<SuccessMessageResponse<CustomerProfileDTO>> getProfile(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) throws IOException {
        CustomerProfileDTO customerProfileDTO = customerService.getCustomerProfile(userDetailsImpl);
        return ResponseEntity.ok(SuccessMessageResponse.success(customerProfileDTO));
    }

    @GetMapping("/addresses")
    public ResponseEntity<SuccessMessageResponse<List<AddressDTO>>> getAddresses(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        List<AddressDTO> addresses = customerService.getCustomerAddresses(userDetailsImpl);
        return ResponseEntity.ok(SuccessMessageResponse.success(addresses));
    }

    @PutMapping("/profile")
    public ResponseEntity<SuccessMessageResponse<String>> updateProfile(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
            @Validated(OnUpdate.class) @ModelAttribute CustomerProfileDTO customerProfileDTO) throws IOException {

        String message = customerService.updateCustomerProfile(userDetailsImpl, customerProfileDTO);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @PostMapping("/addresses")
    public ResponseEntity<SuccessMessageResponse<String>> addAddress(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
            @Validated(OnCreate.class) @RequestBody CustomerAddressDTO customerAddressDTO) {

        String message = customerService.addCustomerAddress(userDetailsImpl, customerAddressDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessMessageResponse.success(message));
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<SuccessMessageResponse<String>> deleteAddress(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
            @PathVariable String addressId) throws BadRequestException {

        String message = customerService.deleteCustomerAddress(userDetailsImpl, addressId);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<SuccessMessageResponse<String>> updateCustomerAddress(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
            @PathVariable String addressId,
            @Validated(OnUpdate.class) @RequestBody CustomerAddressDTO customerAddressDTO) throws BadRequestException {

        String message = customerService.updateCustomerAddress(userDetailsImpl, addressId, customerAddressDTO);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @GetMapping("/categories")
    public ResponseEntity<SuccessMessageResponse<List<CategoryDTO>>> viewCategories(@RequestParam (required = false) String categoryId) throws BadRequestException {
        List<CategoryDTO> categories = categoryService.viewAllCategories(categoryId);
        return ResponseEntity.ok(SuccessMessageResponse.success(categories));
    }

    @GetMapping("/categories/{categoryId}/filtering-details")
    public ResponseEntity<SuccessMessageResponse<FilteringDetailsDTO>> getFilteringDetails(@PathVariable String categoryId) throws BadRequestException {
        FilteringDetailsDTO dto = categoryService.getFilteringDetails(categoryId);
        return ResponseEntity.ok(SuccessMessageResponse.success(dto));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<SuccessMessageResponse<ProductDTO>> viewProduct(@PathVariable String productId) throws BadRequestException {
        ProductDTO dto = productService.viewProductCustomer(productId);
        return ResponseEntity.ok(SuccessMessageResponse.success(dto));
    }

    @GetMapping("/categories/{categoryId}/products")
    public ResponseEntity<SuccessMessageResponse<List<ProductDTO>>> viewAllProductsByCategory(
            @PathVariable String categoryId,
            @RequestParam(required = false, defaultValue = "name") String sort,
            @RequestParam(required = false, defaultValue = "asc") String order,
            @RequestParam(required = false, defaultValue = "10") int max,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false) Map<String, String> filter) throws BadRequestException {

        List<ProductDTO> products = productService.viewAllProducts(categoryId, sort, order, max, offset, filter);
        return ResponseEntity.ok(SuccessMessageResponse.success(products));
    }

    @GetMapping("/products/{productId}/similar")
    public ResponseEntity<SuccessMessageResponse<List<ProductDTO>>> similarProducts(
            @PathVariable String productId,
            @RequestParam(required = false, defaultValue = "name") String sort,
            @RequestParam(required = false, defaultValue = "asc") String order,
            @RequestParam(required = false, defaultValue = "10") int max,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false) Map<String, String> filter) throws BadRequestException {

        List<ProductDTO> products = productService.viewSimilarProducts(productId, sort, order, max, offset, filter);
        return ResponseEntity.ok(SuccessMessageResponse.success(products));
    }

}
