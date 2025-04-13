package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.dto.CustomerResponseDTO;
import com.shopsavvy.shopshavvy.dto.SellerResponseDTO;
import com.shopsavvy.shopshavvy.exception.AlreadyActivatedException;
import com.shopsavvy.shopshavvy.exception.UserNotFoundException;
import com.shopsavvy.shopshavvy.model.users.Customer;
import com.shopsavvy.shopshavvy.model.users.Seller;
import com.shopsavvy.shopshavvy.model.users.User;
import com.shopsavvy.shopshavvy.repository.CustomerRepository;
import com.shopsavvy.shopshavvy.repository.SellerRepository;
import com.shopsavvy.shopshavvy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final CustomerRepository customerRepository;
    private final SellerRepository sellerRepository;

    public String unlockUser(String email){
        User user = userRepository.findByEmail(email);
        if(user == null){
            throw new UserNotFoundException("User with this email ID: " + email + " is not found.");
        }

        if(!user.isLocked()){
            return "User is already unlocked.";
        }
        user.setLocked(false);
        user.setInvalidAttemptCount(0);
        userRepository.save(user);
        return "User is successfully unlocked.";
    }

    public List<CustomerResponseDTO> getAllCustomers(int pageSize, int pageOffset, String sort, String email) {
        Pageable pageable = PageRequest.of(pageOffset, pageSize, Sort.by(sort));
        Page<Customer> customers;

        if (email != null && !email.isEmpty()) {
            customers = customerRepository.findByEmailContainingIgnoreCase(email, pageable);
        } else {
            customers = customerRepository.findAll(pageable);
        }

        return customers.stream()
                .map(customer -> new CustomerResponseDTO(
                        customer.getId(),
                        customer.getFirstName() + " " + (customer.getMiddleName() != null ? customer.getMiddleName() + " " : "") + customer.getLastName(),
                        customer.getEmail(),
                        customer.getIsActive()
                ))
                .collect(Collectors.toList());
    }

    public List<SellerResponseDTO> getAllSellers(int pageSize, int pageOffset, String sort, String email) {
        Pageable pageable = PageRequest.of(pageOffset, pageSize, Sort.by(sort));
        Page<Seller> sellers;

        if (email != null && !email.isEmpty()) {
            sellers = sellerRepository.findByEmailContainingIgnoreCase(email, pageable);
        } else {
            sellers = sellerRepository.findAll(pageable);
        }

        return sellers.stream()
                .map(seller -> new SellerResponseDTO(
                        seller.getId(),
                        seller.getFirstName() + " " + (seller.getMiddleName() != null ? seller.getMiddleName() + " " : "") + seller.getLastName(),
                        seller.getEmail(),
                        seller.getIsActive(),
                        seller.getCompanyName(),
                        seller.getAdresses(),
                        seller.getCompanyContact()
                ))
                .collect(Collectors.toList());
    }

    public String activateCustomer(String customerID) {
        Customer customer = customerRepository.findById(customerID)
                .orElseThrow(() -> new UserNotFoundException("Customer not found with ID: " + customerID));

        if (customer.getIsActive()) {
            throw new AlreadyActivatedException("Customer is already activated. No action performed.");
        }

        customer.setIsActive(true);
        customerRepository.save(customer);

        try {
            emailService.sendVerificationEmail(customer.getEmail(), "Account Activated", "Your account has been successfully activated.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to send activation email.");
        }

        return "Customer account has been successfully activated.";
    }

    public String activateSeller(String sellerID) {
        Seller seller = sellerRepository.findById(sellerID)
                .orElseThrow(() -> new UserNotFoundException("Seller not found with ID: " + sellerID));

        if (seller.getIsActive()) {
            throw new AlreadyActivatedException("Seller is already activated. No action performed.");
        }

        seller.setIsActive(true);
        sellerRepository.save(seller);
        try{
        emailService.sendVerificationEmail(seller.getEmail(), "Account Activated", "Your account has been successfully activated.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to send activation email.");
        }

        return "Seller account has been successfully activated.";
    }

    public String deactivateCustomer(String customerID){
        Customer customer = customerRepository.findById(customerID)
                .orElseThrow(() -> new UserNotFoundException("Customer not found with ID: " + customerID));

        if (!customer.getIsActive()) {
            return "Customer is already deactivated. No action performed.";
        }

        customer.setIsActive(false);
        customerRepository.save(customer);
        try {
            emailService.sendVerificationEmail(customer.getEmail(), "Account Deactivated", "Your account has been successfully deactivated.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to send deactivation email.");
        }

        return "Customer account has been successfully deactivated.";
    }

    public String deactivateSeller(String sellerID) {
        Seller seller = sellerRepository.findById(sellerID)
                .orElseThrow(() -> new UserNotFoundException("Seller not found with ID: " + sellerID));

        if (!seller.getIsActive()) {
            return "Seller is already deactivated. No action performed.";
        }

        seller.setIsActive(false);
        sellerRepository.save(seller);

        try {
            emailService.sendVerificationEmail(seller.getEmail(), "Account Deactivated", "Your account has been successfully deactivated.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to send deactivation email.");
        }

        return "Seller account has been successfully deactivated.";
    }


}
