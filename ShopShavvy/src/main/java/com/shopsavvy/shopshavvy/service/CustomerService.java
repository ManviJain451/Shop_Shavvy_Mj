package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.dto.AddressDTO;
import com.shopsavvy.shopshavvy.dto.CustomerAddressDTO;
import com.shopsavvy.shopshavvy.dto.CustomerUpdateProfileDTO;
import com.shopsavvy.shopshavvy.dto.CustomerViewProfileDTO;
import com.shopsavvy.shopshavvy.exception.UserNotFoundException;
import com.shopsavvy.shopshavvy.model.users.Address;
import com.shopsavvy.shopshavvy.model.users.Customer;
import com.shopsavvy.shopshavvy.repository.CustomerRepository;
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

    public List<AddressDTO> getCustomerAddresses(String accessToken) {
        String email = jwtService.extractUsername(accessToken);
        Customer customer = customerRepository.findByEmail(email)
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

    public void updateCustomerProfile(String accessToken, CustomerUpdateProfileDTO customerUpdateProfileDTO) {
        String email = jwtService.extractUsername(accessToken);
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Customer not found for the provided access token."));

        if (customerUpdateProfileDTO.getFirstName() != null) {
            customer.setFirstName(customerUpdateProfileDTO.getFirstName());
        }
        if (customerUpdateProfileDTO.getLastName() != null) {
            customer.setLastName(customerUpdateProfileDTO.getLastName());
        }
        if (customerUpdateProfileDTO.getMiddleName() != null) {
            customer.setMiddleName(customerUpdateProfileDTO.getMiddleName());
        }
        if (customerUpdateProfileDTO.getContact() != null) {
            customer.setContact(customerUpdateProfileDTO.getContact());
        }

        if (customerUpdateProfileDTO.getProfileImage() != null && !customerUpdateProfileDTO.getProfileImage().isEmpty()) {
            try {
                fileStorageService.saveOrUpdateUserPhoto(customer.getId(), customerUpdateProfileDTO.getProfileImage());
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload profile image.", e);
            }
        }

        customerRepository.save(customer);
    }

    public void addCustomerAddress(String accessToken, AddressDTO addressDTO) {
        String email = jwtService.extractUsername(accessToken);
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Customer not found for the provided access token."));

        Address newAddress = new Address();
        newAddress.setCity(addressDTO.getCity());
        newAddress.setState(addressDTO.getState());
        newAddress.setCountry(addressDTO.getCountry());
        newAddress.setAddressLine(addressDTO.getAddressLine());
        newAddress.setZipCode(addressDTO.getZipCode());
        newAddress.setLabel(addressDTO.getLabel());

        customer.getAddresses().add(newAddress);
        customer.setDefaultAddressId(newAddress.getId());

        customerRepository.save(customer);
    }

    public void deleteCustomerAddress(String accessToken, String addressId) {
        String email = jwtService.extractUsername(accessToken);
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Customer not found for the provided access token."));

        Address addressToDelete = customer.getAddresses().stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Address not found for the provided address ID."));

        customer.getAddresses().remove(addressToDelete);

        if (addressId.equals(customer.getDefaultAddressId())) {
            if (!customer.getAddresses().isEmpty()) {
                customer.setDefaultAddressId(customer.getAddresses().iterator().next().getId());
            } else {
                customer.setDefaultAddressId(null);
            }
        }

        customerRepository.save(customer);
    }

    public void updateCustomerAddress(String accessToken, String addressId, CustomerAddressDTO customerAddressDTO) {
        String email = jwtService.extractUsername(accessToken);
        Customer customer = customerRepository.findByEmail(email)
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
