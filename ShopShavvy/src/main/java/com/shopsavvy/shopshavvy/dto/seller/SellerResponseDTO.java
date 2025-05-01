package com.shopsavvy.shopshavvy.dto.seller;

import com.shopsavvy.shopshavvy.model.user.Address;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class SellerResponseDTO {

    private String id;
    private String fullName;
    private String email;
    private boolean isActive;
    private String companyName;
    private Set<Address> companyAddress;
    private String companyContact;

}