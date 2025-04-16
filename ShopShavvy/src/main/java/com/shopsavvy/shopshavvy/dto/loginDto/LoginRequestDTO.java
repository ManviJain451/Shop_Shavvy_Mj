package com.shopsavvy.shopshavvy.dto.loginDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDTO {

    @NotBlank(message = "{validation.login.email.required}")
    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}", message = "{validation.login.email.format}")
    @Size(max = 255, message = "{validation.login.email.size}")
    private String email;

    @NotBlank(message = "{validation.login.password.required}")
    @Size(min = 8, max = 15, message = "{validation.login.password.size}")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "{validation.login.password.pattern}")
    private String password;
}