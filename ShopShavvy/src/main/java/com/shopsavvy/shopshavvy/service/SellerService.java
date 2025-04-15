package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.dto.addressDto.AddressDTO;
import com.shopsavvy.shopshavvy.dto.sellerDto.SellerProfileDTO;
import com.shopsavvy.shopshavvy.exception.UserNotFoundException;
import com.shopsavvy.shopshavvy.model.users.Address;
import com.shopsavvy.shopshavvy.model.users.Seller;
import com.shopsavvy.shopshavvy.repository.SellerRepository;
import com.shopsavvy.shopshavvy.security.configurations.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final JwtService jwtService;
    private final SellerRepository sellerRepository;
    private final FileStorageService fileStorageService;

    @Value("${file.storage.base-path}")
    private String basePath;

    public SellerProfileDTO getSellerProfile(UserDetailsImpl userDetailsImpl) {
        Seller seller = sellerRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Seller not found for the provided access token."));

        String imageUrl = fileStorageService.getUserImageUrl(seller.getId());

        Set<AddressDTO> addressDTOs = seller.getAddresses().stream()
                .map(address -> new AddressDTO(
                        address.getCity(),
                        address.getState(),
                        address.getCountry(),
                        address.getAddressLine(),
                        address.getZipCode(),
                        address.getLabel()
                ))
                .collect(Collectors.toSet());

        return SellerProfileDTO.builder()
                .id(seller.getId())
                .firstName(seller.getFirstName())
                .middleName(seller.getMiddleName())
                .lastName(seller.getLastName())
                .isActive(seller.getIsActive())
                .companyContact(seller.getCompanyContact())
                .companyName(seller.getCompanyName())
                .gst(seller.getGst())
                .addresses(addressDTOs)
                .imageUrl(imageUrl)
                .build();

    }


    public void updateSellerProfile(UserDetailsImpl userDetailsImpl, SellerProfileDTO sellerProfileDTO) {
        Seller seller = sellerRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Seller not found for the provided access token."));

        if (sellerProfileDTO.getFirstName() != null) {
            seller.setFirstName(sellerProfileDTO.getFirstName());
        }
        if (sellerProfileDTO.getMiddleName() != null) {
            seller.setMiddleName(sellerProfileDTO.getMiddleName());
        }
        if (sellerProfileDTO.getLastName() != null) {
            seller.setLastName(sellerProfileDTO.getLastName());
        }
        if (sellerProfileDTO.getCompanyContact() != null) {
            seller.setCompanyContact(sellerProfileDTO.getCompanyContact());
        }

        if (sellerProfileDTO.getProfileImage() != null && !sellerProfileDTO.getProfileImage().isEmpty()) {
            try {
                fileStorageService.saveOrUpdateUserPhoto(seller.getId(), sellerProfileDTO.getProfileImage());
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload profile image.", e);
            }
        }

        sellerRepository.save(seller);
    }

    public void updateAddress(UserDetailsImpl userDetailsImpl, Long addressId, AddressDTO addressDTO) {
        Seller seller = sellerRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Seller not found for the provided access token."));

        Address address = seller.getAddresses().stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Address not found for the provided address ID."));

        if (addressDTO.getCity() != null) {
            address.setCity(addressDTO.getCity());
        }
        if (addressDTO.getState() != null) {
            address.setState(addressDTO.getState());
        }
        if (addressDTO.getCountry() != null) {
            address.setCountry(addressDTO.getCountry());
        }
        if (addressDTO.getZipCode() != null) {
            address.setZipCode(addressDTO.getZipCode());
        }
        if (addressDTO.getAddressLine() != null) {
            address.setAddressLine(addressDTO.getAddressLine());
        }
        if (addressDTO.getLabel() != null) {
            address.setLabel(addressDTO.getLabel());
        }

        sellerRepository.save(seller);
    }

}