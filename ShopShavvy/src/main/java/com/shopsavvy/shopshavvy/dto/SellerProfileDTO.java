package com.shopsavvy.shopshavvy.dto;

import com.shopsavvy.shopshavvy.model.users.Address;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class SellerProfileDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String imageUrl;
    private Boolean isActive;
    private String companyContact;
    private String companyName;
    private String gst;
    private Set<Address> addresses;
}