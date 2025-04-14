package com.shopsavvy.shopshavvy.dto.customerDto;

import com.shopsavvy.shopshavvy.dto.userDto.UserRegistrationDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRegistrationDTO extends UserRegistrationDTO {

    @Pattern(regexp = "^[0-9]{10}$",
            message = "Contact must be exactly 10 digits")
    private String contact;
}
