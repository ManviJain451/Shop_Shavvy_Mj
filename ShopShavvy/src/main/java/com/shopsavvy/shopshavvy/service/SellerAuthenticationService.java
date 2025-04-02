package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.dto.SellerRegistrationDTO;
import com.shopsavvy.shopshavvy.model.users.Address;
import com.shopsavvy.shopshavvy.model.users.Seller;
import com.shopsavvy.shopshavvy.model.users.User;
import com.shopsavvy.shopshavvy.repository.UserRepository;
import com.shopsavvy.shopshavvy.security.UserDetailsImpl;
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

    public SellerAuthenticationService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.jwtService = jwtService;
    }

    public User signup(SellerRegistrationDTO sellerRegistrationDTO) throws Exception {

        if(userRepository.existsByEmail(sellerRegistrationDTO.getEmail())){
            throw new Exception("Email already exists");
        }
        if (userRepository.existsByCompanyNameIgnoreCase(sellerRegistrationDTO.getCompanyName())) {
            throw new Exception("Company name already exists.");
        }

        if (userRepository.existsByGst(sellerRegistrationDTO.getGst())) {
            throw new Exception("GST already exists.");
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
            throw new Exception("Confirm Password is not same as Password.");
        }

        userRepository.save(seller);

        UserDetailsImpl userDetails = new UserDetailsImpl(seller);
        String token = jwtService.generateToken(userDetails, "activation");

        try {
            emailService.sendActivationLink(sellerRegistrationDTO, token);
        } catch (Exception e) {
            throw new Exception("Mail for activating the account is not send");
        }

        return seller;
    }
}
