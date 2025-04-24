package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.dto.addressDto.AddressDTO;
import com.shopsavvy.shopshavvy.dto.addressDto.CustomerAddressDTO;
import com.shopsavvy.shopshavvy.dto.customerDto.CustomerProfileDTO;
import com.shopsavvy.shopshavvy.exception.UserNotFoundException;
import com.shopsavvy.shopshavvy.model.users.Address;
import com.shopsavvy.shopshavvy.model.users.Customer;
import com.shopsavvy.shopshavvy.repository.*;
import com.shopsavvy.shopshavvy.configuration.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CustomerService {
    private final JwtService jwtService;
    private final CustomerRepository customerRepository;
    private final FileStorageService fileStorageService;
    private final AddressRepository addressRepository;
    private final MessageSource messageSource;
    private final CategoryRepository categoryRepository;
    private final CategoryMetadataFieldValuesRepository categoryMetadataFieldValuesRepository;
    private final ProductRepository productRepository;

    private Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    public CustomerProfileDTO getCustomerProfile(UserDetailsImpl userDetailsImpl) throws IOException {
        log.debug("Getting profile for customer: {}", userDetailsImpl.getUsername());

        Customer customer = customerRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> {
                    log.error("Customer not found with email: {}", userDetailsImpl.getUsername());
                    return new UserNotFoundException(messageSource.getMessage("error.custoomer.not.found", null, getCurrentLocale()));
                });

        String imageUrl = fileStorageService.getUserImageUrl(customer.getId());
        log.debug("Retrieved profile for customer ID: {}", customer.getId());
        return CustomerProfileDTO.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .middleName(customer.getMiddleName())
                .lastName(customer.getLastName())
                .active(customer.getIsActive())
                .contact(customer.getContact())
                .imageUrl(imageUrl)
                .build();
    }

    public List<AddressDTO> getCustomerAddresses(UserDetailsImpl userDetailsImpl) {
        log.debug("Getting addresses for customer: {}", userDetailsImpl.getUsername());

        Customer customer = customerRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> {
                    log.error("Customer not found with email: {}", userDetailsImpl.getUsername());
                    return new UserNotFoundException(messageSource.getMessage("error.customer.not.found.token", null, getCurrentLocale()));
                });

        List<AddressDTO> addresses = customer.getAddresses()
                .stream()
                .filter(address -> !address.isDeleted())
                .map(address -> new AddressDTO(
                        address.getCity(),
                        address.getState(),
                        address.getCountry(),
                        address.getAddressLine(),
                        address.getLabel(),
                        address.getZipCode()
                ))
                .toList();

        log.debug("Retrieved {} addresses for customer ID: {}", addresses.size(), customer.getId());
        return addresses;
    }

    public String updateCustomerProfile(UserDetailsImpl userDetailsImpl, CustomerProfileDTO customerProfileDTO) throws IOException {
        log.debug("Updating profile for customer: {}", userDetailsImpl.getUsername());

        Customer customer = customerRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> {
                    log.error("Customer not found with email: {}", userDetailsImpl.getUsername());
                    return new UserNotFoundException(messageSource.getMessage("error.customer.not.found.token", null, getCurrentLocale()));
                });

        if (customerProfileDTO.getFirstName() != null) {
            customer.setFirstName(customerProfileDTO.getFirstName());
        }
        if (customerProfileDTO.getLastName() != null) {
            customer.setLastName(customerProfileDTO.getLastName());
        }
        if (customerProfileDTO.getMiddleName() != null) {
            customer.setMiddleName(customerProfileDTO.getMiddleName());
        }
        if (customerProfileDTO.getContact() != null) {
            customer.setContact(customerProfileDTO.getContact());
        }

        if (customerProfileDTO.getProfileImage() != null && !customerProfileDTO.getProfileImage().isEmpty()) {
            try {
                fileStorageService.saveOrUpdateUserPhoto(customer.getId(), customerProfileDTO.getProfileImage());
            } catch (Exception e) {
                log.error("Failed to update profile image for customer ID: {}", customer.getId(), e);
                throw e;
            }
        }

        customerRepository.save(customer);
        log.info("Profile updated successfully for customer ID: {}", customer.getId());
        return messageSource.getMessage("success.profile.updated", null, getCurrentLocale());
    }

    public String addCustomerAddress(UserDetailsImpl userDetailsImpl, CustomerAddressDTO customerAddressDTO) {
        log.debug("Adding new address for customer: {}", userDetailsImpl.getUsername());

        Customer customer = customerRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> {
                    log.error("Customer not found with email: {}", userDetailsImpl.getUsername());
                    return new UserNotFoundException(messageSource.getMessage("error.customer.not.found.token", null, getCurrentLocale()));
                });

        Address newAddress = Address.builder()
                .city(customerAddressDTO.getCity())
                .state(customerAddressDTO.getState())
                .country(customerAddressDTO.getCountry())
                .addressLine(customerAddressDTO.getAddressLine())
                .zipCode(customerAddressDTO.getZipCode())
                .label(customerAddressDTO.getLabel())
                .build();

        addressRepository.save(newAddress);
        customer.getAddresses().add(newAddress);
        if (customerAddressDTO.isMakeDefault() || customer.getAddresses().size() == 1) {
            customer.setDefaultAddressId(newAddress.getId());
        }

        customerRepository.save(customer);
        log.info("Address added successfully for customer ID: {} with address ID: {}", customer.getId(), newAddress.getId());
        return messageSource.getMessage("success.address.added", null, getCurrentLocale());
    }

    public String deleteCustomerAddress(UserDetailsImpl userDetailsImpl, String addressId) throws BadRequestException {
        log.debug("Deleting address ID: {} for customer: {}", addressId, userDetailsImpl.getUsername());

        Customer customer = customerRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> {
                    log.error("Customer not found with email: {}", userDetailsImpl.getUsername());
                    return new UserNotFoundException(messageSource.getMessage("error.customer.not.found.token", null, getCurrentLocale()));
                });

        customer.getAddresses().stream()
                .filter(a -> a.getId().equals(addressId) && !a.isDeleted())
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Address not found with ID: {} for customer ID: {}", addressId, customer.getId());
                    return new BadRequestException(messageSource.getMessage("error.address.not.found", null, getCurrentLocale()));
                });

        if (addressId.equals(customer.getDefaultAddressId())) {
            Address newDefaultAddress = customer.getAddresses().stream()
                    .filter(a -> !a.getId().equals(addressId))
                    .findFirst()
                    .orElse(null);
            customer.setDefaultAddressId(newDefaultAddress != null ? newDefaultAddress.getId() : null);
        }

        addressRepository.deleteById(addressId);
        customerRepository.save(customer);
        log.info("Address deleted successfully for customer ID: {} with address ID: {}", customer.getId(), addressId);
        return messageSource.getMessage("success.address.deleted", null, getCurrentLocale());
    }

    public String updateCustomerAddress(UserDetailsImpl userDetailsImpl, String addressId, CustomerAddressDTO customerAddressDTO) throws BadRequestException {
        log.debug("Updating address ID: {} for customer: {}", addressId, userDetailsImpl.getUsername());

        Customer customer = customerRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> {
                    log.error("Customer not found with email: {}", userDetailsImpl.getUsername());
                    return new UserNotFoundException(messageSource.getMessage("error.customer.not.found", null, getCurrentLocale()));
                });

        Address addressToUpdate = customer.getAddresses().stream()
                .filter(addr -> addr.getId().equals(addressId) && !addr.isDeleted())
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Address not found with ID: {} for customer ID: {}", addressId, customer.getId());
                    return new BadRequestException(messageSource.getMessage("error.address.not.found.id", null, getCurrentLocale()));
                });

        if (customerAddressDTO.getCity() != null) addressToUpdate.setCity(customerAddressDTO.getCity());
        if (customerAddressDTO.getState() != null) addressToUpdate.setState(customerAddressDTO.getState());
        if (customerAddressDTO.getCountry() != null) addressToUpdate.setCountry(customerAddressDTO.getCountry());
        if (customerAddressDTO.getAddressLine() != null) addressToUpdate.setAddressLine(customerAddressDTO.getAddressLine());
        if (customerAddressDTO.getZipCode() != null) addressToUpdate.setZipCode(customerAddressDTO.getZipCode());
        if (customerAddressDTO.getLabel() != null) addressToUpdate.setLabel(customerAddressDTO.getLabel());

        if (customerAddressDTO.isMakeDefault()) {
            customer.setDefaultAddressId(addressToUpdate.getId());
        }

        addressRepository.save(addressToUpdate);
        customerRepository.save(customer);
        log.info("Address updated successfully for customer ID: {} with address ID: {}", customer.getId(), addressId);
        return messageSource.getMessage("success.address.updated", null, getCurrentLocale());
    }
}