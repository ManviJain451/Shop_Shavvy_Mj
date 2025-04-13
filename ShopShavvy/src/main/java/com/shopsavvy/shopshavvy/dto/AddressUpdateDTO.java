package com.shopsavvy.shopshavvy.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressUpdateDTO {
    private String city;

    private String state;

    private String country;

    private String addressLine;

    private String zipCode;

    private String label;
}
