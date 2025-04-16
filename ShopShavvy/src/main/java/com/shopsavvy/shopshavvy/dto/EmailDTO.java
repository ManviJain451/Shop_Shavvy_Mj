package com.shopsavvy.shopshavvy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailDTO {

    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}",
            message = "{validation.login.email.format}")
    @NotBlank(message = "{validation.login.email.required}")
    @Size(max = 255, message = "{validation.login.email.size}")
    private String email;
}