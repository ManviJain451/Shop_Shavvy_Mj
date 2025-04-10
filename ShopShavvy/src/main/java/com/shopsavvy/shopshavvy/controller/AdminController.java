package com.shopsavvy.shopshavvy.controller;


import com.shopsavvy.shopshavvy.dto.CustomerResponseDTO;
import com.shopsavvy.shopshavvy.dto.SellerResponseDTO;
import com.shopsavvy.shopshavvy.service.AdminService;
import com.shopsavvy.shopshavvy.service.AuthenticationService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shop-shavvy/admin")
public class AdminController {

    private final AuthenticationService authenticationService;
    private final AdminService adminService;

    @Autowired
    public AdminController(AuthenticationService authenticationService, AdminService adminService){
        this.authenticationService = authenticationService;
        this.adminService = adminService;
    }

    @GetMapping("/hello")
    public String sayHello(String token){
        return "hello admin";
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam String accessToken, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws MessagingException {
        return authenticationService.userLogout(accessToken, httpServletRequest, httpServletResponse);
    }

    @GetMapping("/registered-customers")
    public ResponseEntity<List<CustomerResponseDTO>> getAllCustomers(
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "0") int pageOffset,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(required = false) String email) {
        List<CustomerResponseDTO> customers = adminService.getAllCustomers(pageSize, pageOffset, sort, email);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/registered-sellers")
    public ResponseEntity<List<SellerResponseDTO>> getAllSellers(
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "0") int pageOffset,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(required = false) String email) {
        List<SellerResponseDTO> sellers = adminService.getAllSellers(pageSize, pageOffset, sort, email);
        return ResponseEntity.ok(sellers);
    }

    @PutMapping("/activate-customer")
    public ResponseEntity<String> activateCustomer(@RequestParam String email) throws MessagingException {
        String responseMessage = adminService.activateCustomer(email);
        return ResponseEntity.ok(responseMessage);
    }

    @PutMapping("/activate-seller")
    public ResponseEntity<String> activateSeller(@RequestParam String email) throws MessagingException {
        String responseMessage = adminService.activateSeller(email);
        return ResponseEntity.ok(responseMessage);
    }

    @PutMapping("/deactivate-customer")
    public ResponseEntity<String> deactivateCustomer(@RequestParam String email) {
        String responseMessage = adminService.deactivateCustomer(email);
        return ResponseEntity.ok(responseMessage);
    }

    @PutMapping("/deactivate-seller")
    public ResponseEntity<String> deactivateSeller(@RequestParam String email) {
        String responseMessage = adminService.deactivateSeller(email);
        return ResponseEntity.ok(responseMessage);
    }


}
