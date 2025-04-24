package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.dto.EmailDTO;
import com.shopsavvy.shopshavvy.dto.customerDto.CustomerResponseDTO;
import com.shopsavvy.shopshavvy.dto.sellerDto.SellerResponseDTO;
import com.shopsavvy.shopshavvy.exception.AlreadyActivatedException;
import com.shopsavvy.shopshavvy.exception.UserNotFoundException;
import com.shopsavvy.shopshavvy.model.users.Customer;
import com.shopsavvy.shopshavvy.model.users.Seller;
import com.shopsavvy.shopshavvy.model.users.User;
import com.shopsavvy.shopshavvy.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final CustomerRepository customerRepository;
    private final SellerRepository sellerRepository;
    private final MessageSource messageSource;

    private Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    @Value("${file.storage.base-path}")
    private String basePath;

    public String unlockUser(EmailDTO emailDTO) {
        User user = userRepository.findByEmail(emailDTO.getEmail())
                .orElseThrow(() -> new UserNotFoundException(
                        messageSource.getMessage("user.not.found.with.email", new Object[]{emailDTO.getEmail()}, getCurrentLocale())));

        if (!user.isLocked()) {
            return messageSource.getMessage("user.already.unlocked", null, getCurrentLocale());
        }

        user.setLocked(false);
        user.setInvalidAttemptCount(0);
        userRepository.save(user);
        return messageSource.getMessage("user.unlocked.success", null, getCurrentLocale());
    }

    public List<CustomerResponseDTO> getAllCustomers(int pageSize, int pageOffset, String sort, String email) {
        Pageable pageable = PageRequest.of(pageOffset, pageSize, Sort.by(sort));
        Page<Customer> customers = (email != null && !email.isEmpty()) ?
                customerRepository.findByEmailContainingIgnoreCase(email, pageable) :
                customerRepository.findAll(pageable);

        return customers.stream().map(customer -> new CustomerResponseDTO(
                customer.getId(),
                customer.getFirstName() + " " +
                        (customer.getMiddleName() != null ? customer.getMiddleName() + " " : "") +
                        customer.getLastName(),
                customer.getEmail(),
                customer.getIsActive()
        )).collect(Collectors.toList());
    }

    public List<SellerResponseDTO> getAllSellers(int pageSize, int pageOffset, String sort, String email) {
        Pageable pageable = PageRequest.of(pageOffset, pageSize, Sort.by(sort));
        Page<Seller> sellers = (email != null && !email.isEmpty()) ?
                sellerRepository.findByEmailContainingIgnoreCase(email, pageable) :
                sellerRepository.findAll(pageable);

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
        )).collect(Collectors.toList());
    }

    public String activateCustomer(String customerID) {
        Customer customer = customerRepository.findById(customerID)
                .orElseThrow(() -> new UserNotFoundException(
                        messageSource.getMessage("customer.not.found", new Object[]{customerID}, getCurrentLocale())));

        if (customer.getIsActive()) {
            throw new AlreadyActivatedException(messageSource.getMessage("customer.already.activated", null, getCurrentLocale()));
        }

        customer.setIsActive(true);
        customerRepository.save(customer);


        try {
            emailService.sendVerificationEmail(customer.getEmail(), "Account Activated", "Your account has been successfully activated.");
        } catch (Exception e) {
            throw new RuntimeException(messageSource.getMessage("email.failed.activation", null, getCurrentLocale()));
        }

        return messageSource.getMessage("customer.activated.success", null, getCurrentLocale());
    }

    public String activateSeller(String sellerID) {
        Seller seller = sellerRepository.findById(sellerID)
                .orElseThrow(() -> new UserNotFoundException(
                        messageSource.getMessage("seller.not.found", new Object[]{sellerID}, getCurrentLocale())));

        if (seller.getIsActive()) {
            throw new AlreadyActivatedException(messageSource.getMessage("seller.already.activated", null, getCurrentLocale()));
        }

        seller.setIsActive(true);
        sellerRepository.save(seller);

        try {
            emailService.sendVerificationEmail(seller.getEmail(),
                    "Account Activated", "Your account has been successfully activated.");
        } catch (Exception e) {
            throw new RuntimeException(messageSource.getMessage("email.failed.activation", null, getCurrentLocale()));
        }

        return messageSource.getMessage("seller.activated.success", null, getCurrentLocale());
    }

    public String deactivateCustomer(String customerID) {
        Customer customer = customerRepository.findById(customerID)
                .orElseThrow(() -> new UserNotFoundException(
                        messageSource.getMessage("customer.not.found", new Object[]{customerID}, getCurrentLocale())));

        if (!customer.getIsActive()) {
            return messageSource.getMessage("customer.already.deactivated", null, getCurrentLocale());
        }

        customer.setIsActive(false);
        customerRepository.save(customer);

        try {
            emailService.sendVerificationEmail(customer.getEmail(), "Account Deactivated", "Your account has been successfully deactivated.");
        } catch (Exception e) {
            throw new RuntimeException(messageSource.getMessage("email.failed.deactivation", null, getCurrentLocale()));
        }

        return messageSource.getMessage("customer.deactivated.success", null, getCurrentLocale());
    }

    public String deactivateSeller(String sellerID) {
        Seller seller = sellerRepository.findById(sellerID)
                .orElseThrow(() -> new UserNotFoundException(
                        messageSource.getMessage("seller.not.found", new Object[]{sellerID}, getCurrentLocale())));

        if (!seller.getIsActive()) {
            return messageSource.getMessage("seller.already.deactivated", null, getCurrentLocale());
        }

        seller.setIsActive(false);
        sellerRepository.save(seller);

        try {
            emailService.sendVerificationEmail(seller.getEmail(), "Account Deactivated", "Your account has been successfully deactivated.");
        } catch (Exception e) {
            throw new RuntimeException(messageSource.getMessage("email.failed.deactivation", null, getCurrentLocale()));
        }

        return messageSource.getMessage("seller.deactivated.success", null, getCurrentLocale());
    }

}
