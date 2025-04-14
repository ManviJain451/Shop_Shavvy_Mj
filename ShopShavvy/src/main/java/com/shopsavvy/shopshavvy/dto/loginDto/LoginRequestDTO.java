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

    @NotBlank(message = "Email is required")
    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}", message = "Invalid email format")
    @Size(max = 255, message = "Email must be less than 255 characters")
    private String email;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 8, max = 15, message = "Password must be between 8 and 15 characters long")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "Password must contain at least one lowercase letter, one uppercase letter, one special character, and one number")
    private String password;
}


