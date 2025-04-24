package com.shopsavvy.shopshavvy.controller;

import com.shopsavvy.shopshavvy.dto.addressDto.AddressDTO;
import com.shopsavvy.shopshavvy.dto.categoryDto.CategoryDetailsForSellerDTO;
import com.shopsavvy.shopshavvy.dto.productDto.*;
import com.shopsavvy.shopshavvy.dto.sellerDto.SellerProfileDTO;
import com.shopsavvy.shopshavvy.configuration.UserDetailsImpl;
import com.shopsavvy.shopshavvy.service.*;
import com.shopsavvy.shopshavvy.utilities.SuccessMessageResponse;
import com.shopsavvy.shopshavvy.validation.groups.OnUpdate;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shop-shavvy/seller")
public class SellerController {

    private final AuthenticationService authenticationService;
    private final SellerService sellerService;
    private final CategoryService categoryService;
    private final ProductService productService;
    private final ProductVariationService productVariationService;

    @GetMapping("/hello")
    public String sayHello( String token){
        return "hello seller";
    }

    @PostMapping("/logout")
    public ResponseEntity<SuccessMessageResponse<String>> logout(@RequestParam String accessToken, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws MessagingException {
        String message = authenticationService.userLogout(accessToken, httpServletRequest, httpServletResponse);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @GetMapping("/view-profile")
    public ResponseEntity<SuccessMessageResponse<SellerProfileDTO>> getSellerProfile(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        SellerProfileDTO sellerProfile = sellerService.getSellerProfile(userDetailsImpl);
        return ResponseEntity.ok(SuccessMessageResponse.success(sellerProfile));
    }

    @PutMapping("/update-profile")
    public ResponseEntity<SuccessMessageResponse<String>> updateSellerProfile(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
            @Validated(OnUpdate.class) @ModelAttribute SellerProfileDTO sellerProfileDTO) {

        String message = sellerService.updateSellerProfile(userDetailsImpl, sellerProfileDTO);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @PutMapping("/update-address")
    public ResponseEntity<SuccessMessageResponse<String>> updateAddress(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                                                                        @RequestParam String addressId,
            @Validated(OnUpdate.class) @RequestBody AddressDTO addressUpdateDTO) throws BadRequestException {

        String message = sellerService.updateAddress(userDetailsImpl, addressId, addressUpdateDTO);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @GetMapping("/category")
    public ResponseEntity<List<CategoryDetailsForSellerDTO>> viewCatgeory(){
        List<CategoryDetailsForSellerDTO> categories = categoryService.viewCategory();
        return ResponseEntity.ok(categories);
    }

    @PostMapping("/product")
    public ResponseEntity<SuccessMessageResponse<String>> addProduct(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                                                                     @Valid @RequestBody ProductDTO productDTO) throws BadRequestException {
        String message = productService.addProduct(userDetailsImpl, productDTO);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductDTO> viewProduct(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable String productId) throws BadRequestException {
        ProductDTO dto = productService.viewProduct(userDetails, productId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductDTO>> viewAllProducts(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false, defaultValue = "name") String sort,
            @RequestParam(required = false,defaultValue = "asc") String order,
            @RequestParam(required = false, defaultValue = "10") int max,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false) Map<String, String> filter){
        List<ProductDTO> products = productService.viewAllProducts(userDetails, sort, order, max, offset, filter);
        return ResponseEntity.ok(products);
    }

    @PostMapping("/product/variations")
    public ResponseEntity<SuccessMessageResponse<String>> addProductVariation(@RequestPart("productData") @Valid ProductVariationDTO dto,
            @RequestPart("primaryImage") MultipartFile primaryImage,
            @RequestPart(value = "secondaryImages", required = false) List<MultipartFile> secondaryImages
    ) throws BadRequestException {
        String message = productVariationService.addProductVariations(dto,primaryImage,secondaryImages);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @GetMapping("/product/variations/{id}")
    public ResponseEntity<ProductVariationResponseDTO> viewProductVariation(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("id") String productVariationId) throws BadRequestException {
        ProductVariationResponseDTO dto = productVariationService.viewProductVariation(userDetails, productVariationId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/product/{id}/variations")
    public ResponseEntity<List<ProductVariationResponseDTO>> viewProductVariations(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("id") String productId,
            @RequestParam(required = false, defaultValue = "price") String sort,
            @RequestParam(required = false, defaultValue = "asc") String order,
            @RequestParam(required = false, defaultValue = "10") int max,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false) Map<String, Object> filter) throws BadRequestException {
        List<ProductVariationResponseDTO> variations = productVariationService.viewAllProductVariation(userDetails, productId,
                sort, order, max, offset, filter);
        return ResponseEntity.ok(variations);
    }

    @DeleteMapping("/product")
    public ResponseEntity<SuccessMessageResponse<String>> deleteProduct(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("id") String productId) throws BadRequestException {
        String response = productService.deleteProduct(userDetails, productId);
        return ResponseEntity.ok(SuccessMessageResponse.success(response));
    }

    @PutMapping("/product")
    public ResponseEntity<SuccessMessageResponse<String>> updateProduct(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("id") String productId,
            @Valid @RequestBody ProductUpdateDTO updateDTO) throws BadRequestException {
        String response = productService.updateProduct(userDetails, productId, updateDTO);
        return ResponseEntity.ok(SuccessMessageResponse.success(response));
    }

    @PutMapping(value = "/product/variation", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessMessageResponse<String>> updateProductVariation(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("id") String variationId,
            @RequestPart(value = "data", required = false) ProductVariationUpdateDTO updateDTO,
            @RequestPart(value = "primaryImage", required = false) MultipartFile primaryImage,
            @RequestPart(value = "secondaryImages", required = false) List<MultipartFile> secondaryImages) throws BadRequestException {
        String response = productVariationService.updateProductVariation(userDetails, variationId, updateDTO, primaryImage, secondaryImages);
        return ResponseEntity.ok(SuccessMessageResponse.success(response));
    }

}
