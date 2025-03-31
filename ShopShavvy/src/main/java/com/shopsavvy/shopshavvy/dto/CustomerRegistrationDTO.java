package com.shopsavvy.shopshavvy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRegistrationDTO extends UserRegistrationDTO{

    @NotBlank(message = "Contact is mandatory")
    @Pattern(regexp = "\\d{10}", message = "Contact should be a valid 10-digit phone number")
    private String contact;
}
