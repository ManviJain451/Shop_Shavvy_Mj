package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.exception.DuplicateEntryExistsException;
import com.shopsavvy.shopshavvy.exception.EmailAlreadyExistsException;
import com.shopsavvy.shopshavvy.exception.PasswordMismatchException;
import com.shopsavvy.shopshavvy.dto.SellerRegistrationDTO;
import com.shopsavvy.shopshavvy.model.users.*;
import com.shopsavvy.shopshavvy.repository.AuthTokenRepository;
import com.shopsavvy.shopshavvy.repository.RoleRepository;
import com.shopsavvy.shopshavvy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.Set;


@Service
@RequiredArgsConstructor
public class SellerAuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final AuthTokenRepository authTokenRepository;
    private final RoleRepository roleRepository;

    public String registerSeller(SellerRegistrationDTO sellerRegistrationDTO) throws Exception {

        if(userRepository.existsByEmail(sellerRegistrationDTO.getEmail())){
            throw new EmailAlreadyExistsException("Email already exists");
        }
        if (userRepository.existsByCompanyName(sellerRegistrationDTO.getCompanyName())) {
            throw new DuplicateEntryExistsException("Company name already exists.");
        }

        if (userRepository.existsByGst(sellerRegistrationDTO.getGst())) {
            throw new DuplicateEntryExistsException("GST Number already exists.");
        }

        if (!sellerRegistrationDTO.getConfirmPassword().equals(sellerRegistrationDTO.getPassword())) {
            throw new PasswordMismatchException("Confirm Password is not same as Password.");
        }


        Address address = Address.builder()
                .city(sellerRegistrationDTO.getAddress().getCity())
                .state(sellerRegistrationDTO.getAddress().getState())
                .country(sellerRegistrationDTO.getAddress().getCountry())
                .addressLine(sellerRegistrationDTO.getAddress().getAddressLine())
                .zipCode(sellerRegistrationDTO.getAddress().getZipCode())
                .build();

        Seller seller = Seller.builder()
                .email(sellerRegistrationDTO.getEmail())
                .firstName(sellerRegistrationDTO.getFirstName())
                .middleName(sellerRegistrationDTO.getMiddleName() != null && !sellerRegistrationDTO.getMiddleName().isBlank() ? sellerRegistrationDTO.getMiddleName() : null)
                .lastName(sellerRegistrationDTO.getLastName())
                .password(passwordEncoder.encode(sellerRegistrationDTO.getPassword()))
                .companyContact(sellerRegistrationDTO.getCompanyContact())
                .companyName(sellerRegistrationDTO.getCompanyName())
                .gst(sellerRegistrationDTO.getGst())
                .addresses(Set.of(address))
                .defaultAddressId(address.getId())
                .build();


        Role role = roleRepository.findByAuthority("ROLE_SELLER");
        seller.addRole(role);
        userRepository.save(seller);

        try {
            emailService.sendVerificationEmail(seller.getEmail(), "Account Created", "Seller Account has been created. Waiting for Approval");
        } catch (Exception e) {
            throw new Exception("Confirmation mail for account creation is not send.");
        }

        return "Seller has been registered";
    }
}
