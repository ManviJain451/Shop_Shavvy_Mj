package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.dto.AddressUpdateDTO;
import com.shopsavvy.shopshavvy.dto.SellerViewProfileDTO;
import com.shopsavvy.shopshavvy.dto.SellerUpdateProfileDTO;
import com.shopsavvy.shopshavvy.exception.UserNotFoundException;
import com.shopsavvy.shopshavvy.model.users.Address;
import com.shopsavvy.shopshavvy.model.users.Seller;
import com.shopsavvy.shopshavvy.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final JwtService jwtService;
    private final SellerRepository sellerRepository;
    private final FileStorageService fileStorageService;

    @Value("${file.storage.base-path}")
    private String basePath;

    public SellerViewProfileDTO getSellerProfile(String accessToken) {
        String email = jwtService.extractUsername(accessToken);
        Seller seller = sellerRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Seller not found for the provided access token."));

        String imageUrl = null;
        Path userDirectory = Path.of(basePath, "users", seller.getId());
        try {
            Optional<Path> imageFile = Files.list(userDirectory)
                    .filter(file -> file.getFileName().toString().startsWith(seller.getId() + "."))
                    .findFirst();

            if (imageFile.isPresent()) {
                String fileName = imageFile.get().getFileName().toString();
                imageUrl = "/users/" + seller.getId() + "/" + fileName;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while accessing the user's image directory.", e);
        }

        return new SellerViewProfileDTO(
                seller.getId(),
                seller.getFirstName(),
                seller.getLastName(),
                imageUrl,
                seller.getIsActive(),
                seller.getCompanyContact(),
                seller.getCompanyName(),
                seller.getGst(),
                seller.getAdresses()
        );
    }


    public void updateSellerProfile(String accessToken, SellerUpdateProfileDTO sellerUpdateProfileDTO) {
        String email = jwtService.extractUsername(accessToken);
        Seller seller = sellerRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Seller not found for the provided access token."));

        if (sellerUpdateProfileDTO.getFirstName() != null) {
            seller.setFirstName(sellerUpdateProfileDTO.getFirstName());
        }
        if (sellerUpdateProfileDTO.getMiddleName() != null) {
            seller.setMiddleName(sellerUpdateProfileDTO.getMiddleName());
        }
        if (sellerUpdateProfileDTO.getLastName() != null) {
            seller.setLastName(sellerUpdateProfileDTO.getLastName());
        }
        if (sellerUpdateProfileDTO.getCompanyContact() != null) {
            seller.setCompanyContact(sellerUpdateProfileDTO.getCompanyContact());
        }

        if (sellerUpdateProfileDTO.getProfileImage() != null && !sellerUpdateProfileDTO.getProfileImage().isEmpty()) {
            try {
                fileStorageService.saveOrUpdateUserPhoto(seller.getId(), sellerUpdateProfileDTO.getProfileImage());
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload profile image.", e);
            }
        }

        sellerRepository.save(seller);
    }

    public void updateAddress(String accessToken, Long addressId, AddressUpdateDTO addressUpdateDTO) {
        String email = jwtService.extractUsername(accessToken);
        Seller seller = sellerRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Seller not found for the provided access token."));

        Address address = seller.getAdresses().stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Address not found for the provided address ID."));

        if (addressUpdateDTO.getCity() != null) {
            address.setCity(addressUpdateDTO.getCity());
        }
        if (addressUpdateDTO.getState() != null) {
            address.setState(addressUpdateDTO.getState());
        }
        if (addressUpdateDTO.getCountry() != null) {
            address.setCountry(addressUpdateDTO.getCountry());
        }
        if (addressUpdateDTO.getZipCode() != null) {
            address.setZipCode(addressUpdateDTO.getZipCode());
        }
        if (addressUpdateDTO.getAddressLine() != null) {
            address.setAddressLine(addressUpdateDTO.getAddressLine());
        }
        if (addressUpdateDTO.getLabel() != null) {
            address.setLabel(addressUpdateDTO.getLabel());
        }

        sellerRepository.save(seller);
    }

}