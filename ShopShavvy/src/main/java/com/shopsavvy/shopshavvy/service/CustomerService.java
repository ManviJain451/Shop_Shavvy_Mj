package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.dto.addressDto.AddressDTO;
import com.shopsavvy.shopshavvy.dto.addressDto.CustomerAddressDTO;
import com.shopsavvy.shopshavvy.dto.customerDto.CustomerProfileDTO;
import com.shopsavvy.shopshavvy.exception.UserNotFoundException;
import com.shopsavvy.shopshavvy.model.users.Address;
import com.shopsavvy.shopshavvy.model.users.Customer;
import com.shopsavvy.shopshavvy.repository.AddressRepository;
import com.shopsavvy.shopshavvy.repository.CustomerRepository;
import com.shopsavvy.shopshavvy.security.configurations.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class CustomerService {
    private final JwtService jwtService;
    private final CustomerRepository customerRepository;
    private final FileStorageService fileStorageService;
    private final AddressRepository addressRepository;

    public CustomerProfileDTO getCustomerProfile(UserDetailsImpl userDetailsImpl) {

        Customer customer = customerRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Customer not found"));

        String imageUrl = fileStorageService.getUserImageUrl(customer.getId());
        return CustomerProfileDTO.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .middleName(customer.getMiddleName())
                .lastName(customer.getLastName())
                .isActive(customer.getIsActive())
                .contact(customer.getContact())
                .imageUrl(imageUrl)
                .build();

    }

    public List<AddressDTO> getCustomerAddresses(UserDetailsImpl userDetailsImpl) {
        Customer customer = customerRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Customer not found for the provided access token."));

        return customer.getAddresses().stream()
                .map(address -> new AddressDTO(
                        address.getCity(),
                        address.getState(),
                        address.getCountry(),
                        address.getAddressLine(),
                        address.getLabel(),
                        address.getZipCode()
                ))
                .collect(Collectors.toList());
    }

    public void updateCustomerProfile(UserDetailsImpl userDetailsImpl, CustomerProfileDTO customerProfileDTO) {
        Customer customer = customerRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Customer not found for the provided access token."));

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
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload profile image.", e);
            }
        }

        customerRepository.save(customer);
    }

    public void addCustomerAddress(UserDetailsImpl userDetailsImpl, CustomerAddressDTO customerAddressDTO) {
        Customer customer = customerRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Customer not found for the provided access token."));

        Address newAddress = Address.builder()
                .city(customerAddressDTO.getCity())
                .state(customerAddressDTO.getState())
                .country(customerAddressDTO.getCountry())
                .addressLine(customerAddressDTO.getAddressLine())
                .zipCode(customerAddressDTO.getZipCode())
                .label(customerAddressDTO.getLabel())
                .build();


        customer.getAddresses().add(newAddress);
        if (customerAddressDTO.isMakeDefault() || customer.getAddresses().size() == 1) {
            customer.setDefaultAddressId(newAddress.getId());
        }

        customerRepository.save(customer);
    }

    public void deleteCustomerAddress(UserDetailsImpl userDetailsImpl, String addressId) {
        Customer customer = customerRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Customer not found for the provided access token."));

        customer.getAddresses().stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Address not found for the provided address ID."));

        if (addressId.equals(customer.getDefaultAddressId())) {
            Address newDefaultAddress = customer.getAddresses().stream()
                    .filter(a -> !a.getId().equals(addressId))
                    .findFirst()
                    .orElse(null);
            customer.setDefaultAddressId(newDefaultAddress != null ? newDefaultAddress.getId() : null);
        }

        addressRepository.delete(addressId);
        customerRepository.save(customer);
    }

    public void updateCustomerAddress(UserDetailsImpl userDetailsImpl, String addressId, CustomerAddressDTO customerAddressDTO) {
        Customer customer = customerRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Customer not found."));

        Address addressToUpdate = customer.getAddresses().stream()
                .filter(addr -> addr.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Address not found for the provided ID."));

        if (customerAddressDTO.getCity() != null) addressToUpdate.setCity(customerAddressDTO.getCity());
        if (customerAddressDTO.getState() != null) addressToUpdate.setState(customerAddressDTO.getState());
        if (customerAddressDTO.getCountry() != null) addressToUpdate.setCountry(customerAddressDTO.getCountry());
        if (customerAddressDTO.getAddressLine() != null) addressToUpdate.setAddressLine(customerAddressDTO.getAddressLine());
        if (customerAddressDTO.getZipCode() != null) addressToUpdate.setZipCode(customerAddressDTO.getZipCode());
        if (customerAddressDTO.getLabel() != null) addressToUpdate.setLabel(customerAddressDTO.getLabel());

        if (customerAddressDTO.isMakeDefault()) {
            customer.setDefaultAddressId(addressToUpdate.getId());
        }

        customerRepository.save(customer);
    }

}
