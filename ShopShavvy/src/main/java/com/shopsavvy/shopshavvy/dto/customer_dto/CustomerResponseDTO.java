package com.shopsavvy.shopshavvy.dto.customer_dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CustomerResponseDTO {

    private String id;
    private String fullName;
    private String email;
    private boolean isActive;

}
