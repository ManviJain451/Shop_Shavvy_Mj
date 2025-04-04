package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.Exception.DuplicateEntryExistsException;
import com.shopsavvy.shopshavvy.Exception.EmailAlreadyExistsException;
import com.shopsavvy.shopshavvy.Exception.PasswordMismatchException;
import com.shopsavvy.shopshavvy.dto.SellerRegistrationDTO;
import com.shopsavvy.shopshavvy.model.users.*;
import com.shopsavvy.shopshavvy.repository.AuthTokenRepository;
import com.shopsavvy.shopshavvy.repository.RoleRepository;
import com.shopsavvy.shopshavvy.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;


@Service
public class SellerAuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final AuthTokenRepository authTokenRepository;
    private final RoleRepository roleRepository;

    public SellerAuthenticationService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            JwtService jwtService,
            AuthTokenRepository authTokenRepository,
            RoleRepository roleRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.authTokenRepository = authTokenRepository;
        this.roleRepository = roleRepository;
    }

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

        Seller seller = new Seller();
        seller.setEmail(sellerRegistrationDTO.getEmail());
        seller.setFirstName(sellerRegistrationDTO.getFirstName());
        seller.setLastName(sellerRegistrationDTO.getLastName());
        seller.setPassword(passwordEncoder.encode(sellerRegistrationDTO.getPassword()));
        seller.setCompanyContact(sellerRegistrationDTO.getCompanyContact());
        seller.setCompanyName(sellerRegistrationDTO.getCompanyName());
        seller.setGst(sellerRegistrationDTO.getGst());

        if (sellerRegistrationDTO.getMiddleName() != null && !sellerRegistrationDTO.getMiddleName().isBlank()) {
            seller.setMiddleName(sellerRegistrationDTO.getMiddleName());
        }

        Address address = new Address();
        address.setCity(sellerRegistrationDTO.getCity());
        address.setState(sellerRegistrationDTO.getState());
        address.setCountry(sellerRegistrationDTO.getCountry());
        address.setAddressLine(sellerRegistrationDTO.getAddressLine());
        address.setZipCode(sellerRegistrationDTO.getZipCode());
        seller.setAdresses(Set.of(address));


        if (!sellerRegistrationDTO.getConfirmPassword().equals(sellerRegistrationDTO.getPassword())) {
            throw new PasswordMismatchException("Confirm Password is not same as Password.");
        }

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
