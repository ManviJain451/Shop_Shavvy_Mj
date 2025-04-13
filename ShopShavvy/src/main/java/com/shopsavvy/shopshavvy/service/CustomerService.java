package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.dto.CustomerResponseDTO;
import com.shopsavvy.shopshavvy.dto.CustomerViewProfileDTO;
import com.shopsavvy.shopshavvy.exception.UserNotFoundException;
import com.shopsavvy.shopshavvy.model.users.Customer;
import com.shopsavvy.shopshavvy.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final JwtService jwtService;
    private final CustomerRepository customerRepository;
    private final FileStorageService fileStorageService;

    public CustomerViewProfileDTO getCustomerProfile(String accessToken) {
        String email = jwtService.extractUsername(accessToken);
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Customer not found for the provided access token."));

        String imageUrl = fileStorageService.getUserImageUrl(customer.getId());
        return new CustomerViewProfileDTO(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getIsActive(),
                customer.getContact(),
                imageUrl
        );
    }
}
