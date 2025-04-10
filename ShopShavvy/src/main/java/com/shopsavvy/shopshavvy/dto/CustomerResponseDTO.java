package com.shopsavvy.shopshavvy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class CustomerResponseDTO {

    private String id;
    private String fullName;
    private String email;
    private Boolean isActive;



}
