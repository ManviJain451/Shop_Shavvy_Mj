package com.shopsavvy.shopshavvy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {
    private String city;

    private String state;

    private String country;

    private String addressLine;

    private String zipCode;

    private String label;
}
