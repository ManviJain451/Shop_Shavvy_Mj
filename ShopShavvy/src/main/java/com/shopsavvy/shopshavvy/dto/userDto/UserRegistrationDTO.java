package com.shopsavvy.shopshavvy.dto.userDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegistrationDTO {

    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}", message = "Invalid email format.")
    @NotBlank(message = "Email is mandatory")
    @Size(max = 255, message = "Email must be less than 255 characters")
    private String email;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 8, max = 15, message = "Password must be between 8 and 15 characters long")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "Password must contain at least one lowercase letter, one uppercase letter, one special character, and one number")
    private String password;

    @NotBlank(message = "Confirm Password is mandatory")
    private String confirmPassword;

    @NotBlank(message = "First name is mandatory")
    @Size(min = 2, max = 30, message = "First name must be between 2 and 30 characters")
    @Pattern(regexp = "^[A-Za-z]+([ '-][A-Za-z]+)*$", message = "First name can only contain letters, spaces, hyphens, and apostrophes")
    private String firstName;

    @NotBlank(message = "Last name is mandatory")
    @Size(min = 2, max = 30, message = "Last name must be between 2 and 30 characters")
    @Pattern(regexp = "^[A-Za-z]+([ '-][A-Za-z]+)*$", message = "Last name can only contain letters, spaces, hyphens, and apostrophes")
    private String lastName;


    @Size(min = 2, max = 30, message = "Middle name must be between 2 and 30 characters")
    @Pattern(regexp = "^[A-Za-z]+([ '-][A-Za-z]+)*$", message = "Middle name can only contain letters, spaces, hyphens, and apostrophes")
    private String middleName;

}