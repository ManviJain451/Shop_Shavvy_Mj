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
    public ResponseEntity<Page<CustomerResponseDTO>> getAllCustomers(
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "0") int pageOffset,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(required = false) String email) {
        Page<CustomerResponseDTO> customers = adminService.getAllCustomers(pageSize, pageOffset, sort, email);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/registered-sellers")
    public ResponseEntity<Page<SellerResponseDTO>> getAllSellers(
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "0") int pageOffset,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(required = false) String email) {
        Page<SellerResponseDTO> sellers = adminService.getAllSellers(pageSize, pageOffset, sort, email);
        return ResponseEntity.ok(sellers);
    }
}
