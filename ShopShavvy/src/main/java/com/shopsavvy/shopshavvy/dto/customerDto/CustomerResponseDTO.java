package com.shopsavvy.shopshavvy.dto.customerDto;

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
    private Boolean isActive;

}
