package com.shopsavvy.shopshavvy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailDTO {

    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}", message = "Invalid email format.")
    @NotBlank(message = "Email is mandatory")
    @Size(max = 255, message = "Email must be less than 255 characters")
    private String email;

}
