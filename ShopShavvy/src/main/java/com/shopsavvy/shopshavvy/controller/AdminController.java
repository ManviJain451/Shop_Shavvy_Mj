package com.shopsavvy.shopshavvy.controller;


import com.shopsavvy.shopshavvy.dto.EmailDTO;
import com.shopsavvy.shopshavvy.dto.customerDto.CustomerResponseDTO;
import com.shopsavvy.shopshavvy.dto.sellerDto.SellerResponseDTO;
import com.shopsavvy.shopshavvy.service.AdminService;
import com.shopsavvy.shopshavvy.service.AuthenticationService;
import com.shopsavvy.shopshavvy.utilities.SuccessMessageResponse;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shop-shavvy/admin")
public class AdminController {

    private final AuthenticationService authenticationService;
    private final AdminService adminService;

    @GetMapping("/hello")
    public String sayHello(String token){
        return "hello admin";
    }

    @PostMapping("/logout")
    public ResponseEntity<SuccessMessageResponse<String>> logout(@RequestParam String accessToken, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws MessagingException {
        String message = authenticationService.userLogout(accessToken, httpServletRequest, httpServletResponse);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @PutMapping("/unlock/user")
    public ResponseEntity<SuccessMessageResponse<String>> unlockUser(@Valid @RequestParam EmailDTO emailDTO){
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
    public ResponseEntity<SuccessMessageResponse<String>> activateCustomer(@RequestParam String customerID) throws MessagingException {
        String responseMessage = adminService.activateCustomer(customerID);
        return ResponseEntity.ok(SuccessMessageResponse.success(responseMessage));
    }

    @PutMapping("/activate-seller")
    public ResponseEntity<SuccessMessageResponse<String>> activateSeller(@RequestParam String sellerID) throws MessagingException {
        String responseMessage = adminService.activateSeller(sellerID);
        return ResponseEntity.ok(SuccessMessageResponse.success(responseMessage));
    }

    @PutMapping("/deactivate-customer")
    public ResponseEntity<SuccessMessageResponse<String>> deactivateCustomer(@RequestParam String customerID) {
        String responseMessage = adminService.deactivateCustomer(customerID);
        return ResponseEntity.ok(SuccessMessageResponse.success(responseMessage));
    }

    @PutMapping("/deactivate-seller")
    public ResponseEntity<SuccessMessageResponse<String>> deactivateSeller(@RequestParam String sellerID) {
        String responseMessage = adminService.deactivateSeller(sellerID);
        return ResponseEntity.ok(SuccessMessageResponse.success(responseMessage));
    }


}
