package com.shopsavvy.shopshavvy.dto;

import com.shopsavvy.shopshavvy.model.users.Address;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class SellerResponseDTO {
    private String id;
    private String fullName;
    private String email;
    private Boolean isActive;
    private String companyName;
    private Set<Address> companyAddress;
    private String companyContact;
}