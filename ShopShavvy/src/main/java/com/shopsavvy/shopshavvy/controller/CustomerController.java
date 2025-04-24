package com.shopsavvy.shopshavvy.controller;

import com.shopsavvy.shopshavvy.dto.addressDto.AddressDTO;
import com.shopsavvy.shopshavvy.dto.addressDto.CustomerAddressDTO;
import com.shopsavvy.shopshavvy.dto.categoryDto.CategoryDTO;
import com.shopsavvy.shopshavvy.dto.categoryDto.FilteringDetailsDTO;
import com.shopsavvy.shopshavvy.dto.customerDto.CustomerProfileDTO;
import com.shopsavvy.shopshavvy.dto.productDto.ProductDTO;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shop-shavvy/customer")
public class CustomerController {

    private final AuthenticationService authenticationService;
    private final CustomerService customerService;
    private final CategoryService categoryService;
    private final ProductService productService;

    @GetMapping("/hello")
    public String sayHello(String token){
        return "hello customer";
    }

    @PostMapping("/logout")
    public ResponseEntity<SuccessMessageResponse<String>> logout(@RequestParam  String accessToken,
                                                                HttpServletRequest httpServletRequest,
                                                                HttpServletResponse httpServletResponse) throws MessagingException {
        String message = authenticationService.userLogout(accessToken, httpServletRequest, httpServletResponse);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @GetMapping("/view-profile")
    public ResponseEntity<SuccessMessageResponse<CustomerProfileDTO>> getProfile(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) throws IOException {
        CustomerProfileDTO customerProfileDTO = customerService.getCustomerProfile(userDetailsImpl);
        return ResponseEntity.ok(SuccessMessageResponse.success(customerProfileDTO));
    }

    @GetMapping("/view-addresses")
    public ResponseEntity<SuccessMessageResponse<List<AddressDTO>>> getAddresses(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        List<AddressDTO> addresses = customerService.getCustomerAddresses(userDetailsImpl);
        return ResponseEntity.ok(SuccessMessageResponse.success(addresses));
    }

    @PutMapping("/update-profile")
    public ResponseEntity<SuccessMessageResponse<String>> updateProfile(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
            @Validated(OnUpdate.class) @ModelAttribute CustomerProfileDTO customerProfileDTO) throws IOException {

        String message = customerService.updateCustomerProfile(userDetailsImpl, customerProfileDTO);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @PostMapping("/address")
    public ResponseEntity<SuccessMessageResponse<String>> addAddress(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
            @Validated(OnCreate.class) @RequestBody CustomerAddressDTO customerAddressDTO) {

        String message = customerService.addCustomerAddress(userDetailsImpl, customerAddressDTO);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @DeleteMapping("/address")
    public ResponseEntity<SuccessMessageResponse<String>> deleteAddress(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
            @RequestParam String addressId) throws BadRequestException {

        String message = customerService.deleteCustomerAddress(userDetailsImpl, addressId);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @PutMapping("/address")
    public ResponseEntity<SuccessMessageResponse<String>> updateCustomerAddress(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
            @RequestParam String addressId,
            @Validated(OnUpdate.class) @RequestBody CustomerAddressDTO customerAddressDTO) throws BadRequestException {

        String message = customerService.updateCustomerAddress(userDetailsImpl, addressId, customerAddressDTO);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> viewCategories(@RequestParam(required = false) String categoryId) throws BadRequestException {
        List<CategoryDTO> categories = categoryService.viewAllCategories(categoryId);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/filtering-details")
    public ResponseEntity<FilteringDetailsDTO> getFilteringDetails(@RequestParam String categoryId) throws BadRequestException {
        FilteringDetailsDTO dto = categoryService.getFilteringDetails(categoryId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductDTO> viewProduct(@PathVariable String productId) throws BadRequestException {
        ProductDTO dto = productService.viewProductCustomer(productId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductDTO>> viewAllProducts(
            @RequestParam String categoryId,
            @RequestParam(required = false, defaultValue = "name") String sort,
            @RequestParam(required = false, defaultValue = "asc") String order,
            @RequestParam(required = false, defaultValue = "10") int max,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false) Map<String, String> filter) throws BadRequestException {

        List<ProductDTO> products = productService.viewAllProducts(categoryId, sort, order, max, offset, filter);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/similar")
    public ResponseEntity<List<ProductDTO>> similarProducts(
            @RequestParam String productId,
            @RequestParam(required = false, defaultValue = "name") String sort,
            @RequestParam(required = false, defaultValue = "asc") String order,
            @RequestParam(required = false, defaultValue = "10") int max,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false) Map<String, String> filter) throws BadRequestException {

        List<ProductDTO> products = productService.viewSimilarProducts(productId, sort, order, max, offset, filter);
        return ResponseEntity.ok(products);
    }


}
