package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.configuration.UserDetailsImpl;
import com.shopsavvy.shopshavvy.dto.EmailDTO;
import com.shopsavvy.shopshavvy.dto.customer.CustomerResponseDTO;
import com.shopsavvy.shopshavvy.dto.seller.SellerResponseDTO;
import com.shopsavvy.shopshavvy.dto.user.UserProfileDTO;
import com.shopsavvy.shopshavvy.exception.AlreadyActivatedException;
import com.shopsavvy.shopshavvy.exception.UserNotFoundException;
import com.shopsavvy.shopshavvy.model.user.Customer;
import com.shopsavvy.shopshavvy.model.user.Seller;
import com.shopsavvy.shopshavvy.model.user.User;
import com.shopsavvy.shopshavvy.repository.*;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final CustomerRepository customerRepository;
    private final SellerRepository sellerRepository;
    private final MessageSource messageSource;
    private final FileStorageService fileStorageService;

    private Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    @Value("${file.storage.base-path}")
    private String basePath;

    public UserProfileDTO getProfile(UserDetailsImpl userDetails) throws IOException {
        User admin = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("user.not.found", null, getCurrentLocale())));

        String imageUrl = fileStorageService.getUserImageUrl(admin.getId());
        return UserProfileDTO.builder()
                .id(admin.getId())
                .firstName(admin.getFirstName())
                .middleName(admin.getMiddleName())
                .lastName(admin.getLastName())
                .imageUrl(imageUrl)
                .active(admin.getIsActive())
                .build();
    }

    public String updateProfile(UserDetailsImpl userDetailsImpl, UserProfileDTO userProfileDTO) throws IOException {
        log.debug("Updating profile for admin: {}", userDetailsImpl.getUsername());

        User admin = userRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> {
                    log.error("Customer not found with email: {}", userDetailsImpl.getUsername());
                    return new UserNotFoundException(messageSource.getMessage("user.not.found", null, getCurrentLocale()));
                });

        if (userProfileDTO.getFirstName() != null) {
            admin.setFirstName(userProfileDTO.getFirstName());
        }
        if (userProfileDTO.getLastName() != null) {
            admin.setLastName(userProfileDTO.getLastName());
        }
        if (userProfileDTO.getMiddleName() != null) {
            admin.setMiddleName(userProfileDTO.getMiddleName());
        }

        if (userProfileDTO.getProfileImage() != null && !userProfileDTO.getProfileImage().isEmpty()) {
            try {
                fileStorageService.saveOrUpdateUserPhoto(admin.getId(), userProfileDTO.getProfileImage());
            } catch (Exception e) {
                log.error("Failed to update profile image for customer ID: {}", admin.getId(), e);
                throw e;
            }
        }
        userRepository.save(admin);
        log.info("Profile updated successfully for customer ID: {}", admin.getId());
        return messageSource.getMessage("success.profile.updated", null, getCurrentLocale());
    }


    public String unlockUser(EmailDTO emailDTO) throws MessagingException {
        log.info("Attempting to unlock user with email: {}", emailDTO.getEmail());
        User user = userRepository.findByEmail(emailDTO.getEmail())
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", emailDTO.getEmail());
                    return new UserNotFoundException(
                            messageSource.getMessage("user.not.found.with.email", new Object[]{emailDTO.getEmail()}, getCurrentLocale()));

                });
        if (!user.isLocked()) {
            log.info("User with email {} is already unlocked", emailDTO.getEmail());
            return messageSource.getMessage("user.already.unlocked", null, getCurrentLocale());
        }

        user.setLocked(false);
        user.setInvalidAttemptCount(0);
        userRepository.save(user);
        emailService.sendVerificationEmail(user.getEmail(), "Account Unlocked", "Your account has been unlocked. Now, you can login.");

        log.info("Successfully unlocked user with email: {}", emailDTO.getEmail());
        return messageSource.getMessage("user.unlocked.success", null, getCurrentLocale());
    }

    public List<CustomerResponseDTO> getAllCustomers(int pageSize, int pageOffset, String sort, String email) {
        log.info("Fetching customers with pageSize: {}, pageOffset: {}, sort: {}, email filter: {}",
                pageSize, pageOffset, sort, email);
        Pageable pageable = PageRequest.of(pageOffset, pageSize, Sort.by(sort));
        Page<Customer> customers = (email != null && !email.isEmpty()) ?
                customerRepository.findByEmailContainingIgnoreCase(email, pageable) :
                customerRepository.findAll(pageable);

        log.debug("Found {} customers", customers.getTotalElements());
        return customers.stream().map(customer -> new CustomerResponseDTO(
                customer.getId(),
                customer.getFirstName() + " " +
                        (customer.getMiddleName() != null ? customer.getMiddleName() + " " : "") +
                        customer.getLastName(),
                customer.getEmail(),
                customer.getIsActive()
        )).toList();
    }

    public List<SellerResponseDTO> getAllSellers(int pageSize, int pageOffset, String sort, String email) {
        log.info("Fetching sellers with pageSize: {}, pageOffset: {}, sort: {}, email filter: {}",
                pageSize, pageOffset, sort, email);
        Pageable pageable = PageRequest.of(pageOffset, pageSize, Sort.by(sort));
        Page<Seller> sellers = (email != null && !email.isEmpty()) ?
                sellerRepository.findByEmailContainingIgnoreCase(email, pageable) :
                sellerRepository.findAll(pageable);

        log.debug("Found {} sellers", sellers.getTotalElements());
        return sellers.stream().map(seller -> new SellerResponseDTO(
                seller.getId(),
                seller.getFirstName() + " " +
                        (seller.getMiddleName() != null ? seller.getMiddleName() + " " : "") +
                        seller.getLastName(),
                seller.getEmail(),
                seller.getIsActive(),
                seller.getCompanyName(),
                seller.getAddresses(),
                seller.getCompanyContact()
        )).toList();
    }

    public String activateCustomer(String customerID) {
        log.info("Attempting to activate customer with ID: {}", customerID);
        Customer customer = customerRepository.findById(customerID)
                .orElseThrow(() -> {
                    log.error("Customer not found with ID: {}", customerID);
                    return new UserNotFoundException(
                            messageSource.getMessage("customer.not.found", new Object[]{customerID}, getCurrentLocale()));
                });

        if (Boolean.TRUE.equals(customer.getIsActive())) {
            log.warn("Customer with ID {} is already activated", customerID);
            throw new AlreadyActivatedException(messageSource.getMessage("customer.already.activated", null, getCurrentLocale()));
        }
        customer.setIsActive(true);
        customerRepository.save(customer);
        log.info("Customer with ID {} has been activated", customerID);


        try {
            log.debug("Sending activation email to customer: {}", customer.getEmail());
            emailService.sendVerificationEmail(customer.getEmail(), "Account Activated", "Your account has been successfully activated.");
        } catch (Exception e) {
            log.error("Failed to send activation email to customer: {}", customer.getEmail(), e);
            throw new RuntimeException(messageSource.getMessage("email.failed.activation", null, getCurrentLocale()));
        }

        return messageSource.getMessage("customer.activated.success", null, getCurrentLocale());
    }

    public String activateSeller(String sellerID) {
        log.info("Attempting to activate seller with ID: {}", sellerID);
        Seller seller = sellerRepository.findById(sellerID)
                .orElseThrow(() -> {
                    log.error("Seller not found with ID: {}", sellerID);
                    return new UserNotFoundException(
                            messageSource.getMessage("seller.not.found", new Object[]{sellerID}, getCurrentLocale()));
                });
        if (Boolean.TRUE.equals(seller.getIsActive())) {
            log.warn("Seller with ID {} is already activated", sellerID);
            throw new AlreadyActivatedException(messageSource.getMessage("seller.already.activated", null, getCurrentLocale()));
        }

        seller.setIsActive(true);
        sellerRepository.save(seller);
        log.info("Seller with ID {} has been activated", sellerID);

        try {
            log.debug("Sending activation email to seller: {}", seller.getEmail());
            emailService.sendVerificationEmail(seller.getEmail(),
                    "Account Activated", "Your account has been successfully activated.");
        } catch (Exception e) {
            log.error("Failed to send activation email to seller: {}", seller.getEmail(), e);
            throw new RuntimeException(messageSource.getMessage("email.failed.activation", null, getCurrentLocale()));
        }

        return messageSource.getMessage("seller.activated.success", null, getCurrentLocale());
    }

    public String deactivateCustomer(String customerID) {
        log.info("Attempting to deactivate customer with ID: {}", customerID);
        Customer customer = customerRepository.findById(customerID)
                .orElseThrow(() -> {
                    log.error("Customer not found with ID: {}", customerID);
                    return new UserNotFoundException(
                            messageSource.getMessage("customer.not.found", new Object[]{customerID}, getCurrentLocale()));
                });

        if (Boolean.FALSE.equals(customer.getIsActive())) {
            log.info("Customer with ID {} is already deactivated", customerID);
            return messageSource.getMessage("customer.already.deactivated", null, getCurrentLocale());
        }

        customer.setIsActive(false);
        customerRepository.save(customer);
        log.info("Customer with ID {} has been deactivated", customerID);

        try {
            log.debug("Sending deactivation email to customer: {}", customer.getEmail());
            emailService.sendVerificationEmail(customer.getEmail(), "Account Deactivated", "Your account has been successfully deactivated.");
        } catch (Exception e) {
            log.error("Failed to send deactivation email to customer: {}", customer.getEmail(), e);
            throw new RuntimeException(messageSource.getMessage("email.failed.deactivation", null, getCurrentLocale()));
        }

        return messageSource.getMessage("customer.deactivated.success", null, getCurrentLocale());
    }

    public String deactivateSeller(String sellerID) {
        log.info("Attempting to deactivate seller with ID: {}", sellerID);
        Seller seller = sellerRepository.findById(sellerID)
                .orElseThrow(() -> {
                    log.error("Seller not found with ID: {}", sellerID);
                    return new UserNotFoundException(
                            messageSource.getMessage("seller.not.found", new Object[]{sellerID}, getCurrentLocale()));
                });

        if (Boolean.FALSE.equals(seller.getIsActive())) {
            log.info("Seller with ID {} is already deactivated", sellerID);
            return messageSource.getMessage("seller.already.deactivated", null, getCurrentLocale());
        }

        seller.setIsActive(false);
        sellerRepository.save(seller);
        log.info("Seller with ID {} has been deactivated", sellerID);

        try {
            log.debug("Sending deactivation email to seller: {}", seller.getEmail());
            emailService.sendVerificationEmail(seller.getEmail(), "Account Deactivated", "Your account has been successfully deactivated.");
        } catch (Exception e) {
            log.error("Failed to send deactivation email to seller: {}", seller.getEmail(), e);
            throw new RuntimeException(messageSource.getMessage("email.failed.deactivation", null, getCurrentLocale()));
        }

        return messageSource.getMessage("seller.deactivated.success", null, getCurrentLocale());
    }

}
