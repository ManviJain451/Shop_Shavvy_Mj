package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.dto.address_dto.AddressDTO;
import com.shopsavvy.shopshavvy.dto.seller_dto.SellerProfileDTO;
import com.shopsavvy.shopshavvy.exception.UserNotFoundException;
import com.shopsavvy.shopshavvy.model.users.Address;
import com.shopsavvy.shopshavvy.model.users.Seller;
import com.shopsavvy.shopshavvy.repository.*;
import com.shopsavvy.shopshavvy.configuration.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
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
public class SellerService {
    private final SellerRepository sellerRepository;
    private final FileStorageService fileStorageService;
    private final MessageSource messageSource;

    private Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }


    @Value("${file.storage.base-path}")
    private String basePath;

    public SellerProfileDTO getSellerProfile(UserDetailsImpl userDetailsImpl) throws IOException {
        Seller seller = sellerRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> new UserNotFoundException(
                        messageSource.getMessage("error.seller.not.found.token", null, getCurrentLocale())));

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
                .active(seller.getIsActive())
                .companyContact(seller.getCompanyContact())
                .companyName(seller.getCompanyName())
                .gst(seller.getGst())
                .addresses(addressDTOs)
                .imageUrl(imageUrl)
                .build();
    }

    public String updateSellerProfile(UserDetailsImpl userDetailsImpl, SellerProfileDTO sellerProfileDTO) {
        Seller seller = sellerRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> new UserNotFoundException(
                        messageSource.getMessage("error.seller.not.found.token", null, getCurrentLocale())));

        updateSellerFields(seller, sellerProfileDTO);

        if (sellerProfileDTO.getProfileImage() != null && !sellerProfileDTO.getProfileImage().isEmpty()) {
            try {
                fileStorageService.saveOrUpdateUserPhoto(seller.getId(), sellerProfileDTO.getProfileImage());
            } catch (IOException e) {
                throw new RuntimeException(
                        messageSource.getMessage("error.profile.image.upload", null, getCurrentLocale()), e);
            }
        }

        sellerRepository.save(seller);
        return messageSource.getMessage("success.seller.profile.updated", null, getCurrentLocale());
    }

    public String updateAddress(UserDetailsImpl userDetailsImpl, String addressId, AddressDTO addressDTO) throws BadRequestException {
        Seller seller = sellerRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> new UserNotFoundException(
                        messageSource.getMessage("error.seller.not.found.token", null, getCurrentLocale())));

        Address address = seller.getAddresses().stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(
                        messageSource.getMessage("error.address.not.found.id", null, getCurrentLocale())));

        updateAddressFields(address, addressDTO);
        sellerRepository.save(seller);
        return messageSource.getMessage("success.address.updated", null, getCurrentLocale());
    }

    private void updateSellerFields(Seller seller, SellerProfileDTO dto) {
        if (dto.getFirstName() != null) seller.setFirstName(dto.getFirstName());
        if (dto.getMiddleName() != null) seller.setMiddleName(dto.getMiddleName());
        if (dto.getLastName() != null) seller.setLastName(dto.getLastName());
        if (dto.getCompanyContact() != null) seller.setCompanyContact(dto.getCompanyContact());
    }

    private void updateAddressFields(Address address, AddressDTO dto) {
        if (dto.getCity() != null) address.setCity(dto.getCity());
        if (dto.getState() != null) address.setState(dto.getState());
        if (dto.getCountry() != null) address.setCountry(dto.getCountry());
        if (dto.getZipCode() != null) address.setZipCode(dto.getZipCode());
        if (dto.getAddressLine() != null) address.setAddressLine(dto.getAddressLine());
        if (dto.getLabel() != null) address.setLabel(dto.getLabel());
    }

}

