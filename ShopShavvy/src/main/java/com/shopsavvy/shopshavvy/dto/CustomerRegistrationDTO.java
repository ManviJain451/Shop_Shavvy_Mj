package com.shopsavvy.shopshavvy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRegistrationDTO extends UserRegistrationDTO{

    @NotBlank(message = "Contact is mandatory")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid phone number. Must be a 10-digit Indian number starting with 6-9")
    private String contact;
}
