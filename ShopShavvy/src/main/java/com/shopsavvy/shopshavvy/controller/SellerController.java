package com.shopsavvy.shopshavvy.controller;

import com.shopsavvy.shopshavvy.dto.address.AddressDTO;
import com.shopsavvy.shopshavvy.dto.category.CategoryDetailsForSellerDTO;
import com.shopsavvy.shopshavvy.dto.product.*;
import com.shopsavvy.shopshavvy.dto.seller.SellerProfileDTO;
import com.shopsavvy.shopshavvy.configuration.UserDetailsImpl;
import com.shopsavvy.shopshavvy.service.*;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sellers")
public class SellerController {

    private final AuthenticationService authenticationService;
    private final SellerService sellerService;
    private final CategoryService categoryService;
    private final ProductService productService;
    private final ProductVariationService productVariationService;

    @PostMapping("/logout")
    public ResponseEntity<SuccessMessageResponse<String>> logout(@RequestParam String accessToken, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws MessagingException {
        String message = authenticationService.userLogout(accessToken, httpServletRequest, httpServletResponse);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @GetMapping("/profile")
    public ResponseEntity<SuccessMessageResponse<SellerProfileDTO>> getSellerProfile(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) throws IOException {
        SellerProfileDTO sellerProfile = sellerService.getSellerProfile(userDetailsImpl);
        return ResponseEntity.ok(SuccessMessageResponse.success(sellerProfile));
    }

    @PutMapping("/profile")
    public ResponseEntity<SuccessMessageResponse<String>> updateSellerProfile(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
            @Validated(OnUpdate.class) @ModelAttribute SellerProfileDTO sellerProfileDTO) {

        String message = sellerService.updateSellerProfile(userDetailsImpl, sellerProfileDTO);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<SuccessMessageResponse<String>> updateAddress(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                                                                        @PathVariable String addressId,
            @Validated(OnUpdate.class) @RequestBody AddressDTO addressUpdateDTO) throws BadRequestException {

        String message = sellerService.updateAddress(userDetailsImpl, addressId, addressUpdateDTO);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @GetMapping("/categories")
    public ResponseEntity<SuccessMessageResponse<List<CategoryDetailsForSellerDTO>>> viewCatgeories(){
        List<CategoryDetailsForSellerDTO> categories = categoryService.viewCategory();
        return ResponseEntity.ok(SuccessMessageResponse.success(categories));
    }

    @PostMapping("/products")
    public ResponseEntity<SuccessMessageResponse<String>> addProduct(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                                                                     @Valid @RequestBody ProductDTO productDTO) throws BadRequestException, SendFailedException {
        String message = productService.addProduct(userDetailsImpl, productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessMessageResponse.success(message));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<SuccessMessageResponse<ProductDTO>> viewProduct(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable String productId) throws BadRequestException {
        ProductDTO dto = productService.viewProduct(userDetails, productId);
        return ResponseEntity.ok(SuccessMessageResponse.success(dto));
    }

    @GetMapping("/products")
    public ResponseEntity<SuccessMessageResponse<List<ProductDTO>>> viewAllProducts(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false, defaultValue = "name") String sort,
            @RequestParam(required = false,defaultValue = "asc") String order,
            @RequestParam(required = false, defaultValue = "10") int max,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false) Map<String, String> filter){
        List<ProductDTO> products = productService.viewAllProducts(userDetails, sort, order, max, offset, filter);
        return ResponseEntity.ok(SuccessMessageResponse.success(products));
    }

    @PostMapping("/products/variations")
    public ResponseEntity<SuccessMessageResponse<String>> addProductVariation(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestPart("productData") @Valid ProductVariationDTO dto,
            @RequestPart("primaryImage") MultipartFile primaryImage,
            @RequestPart(value = "secondaryImages", required = false) List<MultipartFile> secondaryImages
    ) throws IOException {
    String message = productVariationService.addProductVariations(userDetails, dto,primaryImage,secondaryImages);
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessMessageResponse.success(message));
    }

    @GetMapping("/products/variations/{id}")
    public ResponseEntity<SuccessMessageResponse<ProductVariationResponseDTO>> viewProductVariation(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("id") String productVariationId) throws IOException {
        ProductVariationResponseDTO dto = productVariationService.viewProductVariation(userDetails, productVariationId);
        return ResponseEntity.ok(SuccessMessageResponse.success(dto));
    }

    @GetMapping("/products/{id}/variations")
    public ResponseEntity<SuccessMessageResponse<List<ProductVariationResponseDTO>>> viewProductVariations(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("id") String productId,
            @RequestParam(required = false, defaultValue = "price") String sort,
            @RequestParam(required = false, defaultValue = "asc") String order,
            @RequestParam(required = false, defaultValue = "10") int max,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false) Map<String, Object> filter) throws BadRequestException {
        List<ProductVariationResponseDTO> variations = productVariationService.viewAllProductVariation(userDetails, productId,
                sort, order, max, offset, filter);
        return ResponseEntity.ok(SuccessMessageResponse.success(variations));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<SuccessMessageResponse<String>> deleteProduct(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("id") String productId) throws BadRequestException {
        String response = productService.deleteProduct(userDetails, productId);
        return ResponseEntity.ok(SuccessMessageResponse.success(response));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<SuccessMessageResponse<String>> updateProduct(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("id") String productId,
            @Valid @RequestBody ProductUpdateDTO updateDTO) throws BadRequestException {
        String response = productService.updateProduct(userDetails, productId, updateDTO);
        return ResponseEntity.ok(SuccessMessageResponse.success(response));
    }

    @PutMapping(value = "/products/variations/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessMessageResponse<String>> updateProductVariation(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("id") String variationId,
            @RequestPart(value = "data", required = false) ProductVariationUpdateDTO updateDTO,
            @RequestPart(value = "primaryImage", required = false) MultipartFile primaryImage,
            @RequestPart(value = "secondaryImages", required = false) List<MultipartFile> secondaryImages) throws BadRequestException {
        String response = productVariationService.updateProductVariation(userDetails, variationId, updateDTO, primaryImage, secondaryImages);
        return ResponseEntity.ok(SuccessMessageResponse.success(response));
    }

}
