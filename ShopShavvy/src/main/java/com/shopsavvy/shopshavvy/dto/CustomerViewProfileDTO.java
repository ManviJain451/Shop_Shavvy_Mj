package com.shopsavvy.shopshavvy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CustomerViewProfileDTO {
    String id;
    String firstName;
    String lastName;
    boolean isActive;
    String contact;
    String imageUrl;
}
