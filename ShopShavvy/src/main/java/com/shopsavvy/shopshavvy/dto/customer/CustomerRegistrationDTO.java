package com.shopsavvy.shopshavvy.dto.customer;

import com.shopsavvy.shopshavvy.dto.user.UserRegistrationDTO;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRegistrationDTO extends UserRegistrationDTO {

    @Pattern(regexp = "^[0-9]{10}$",
            message = "{validation.customer.contact.pattern}")
    private String contact;
}
