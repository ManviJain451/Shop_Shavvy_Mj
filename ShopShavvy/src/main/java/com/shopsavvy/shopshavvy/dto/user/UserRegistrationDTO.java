package com.shopsavvy.shopshavvy.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegistrationDTO {

    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}", message = "{validation.login.email.format}")
    @NotBlank(message = "{validation.login.email.required}")
    @Size(max = 255, message = "{validation.login.email.size}")
    private String email;

    @NotBlank(message = "{validation.password.required}")
    @Size(min = 8, max = 15, message = "{validation.password.size}")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "{validation.password.pattern}")
    private String password;

    @NotBlank(message = "{validation.password.confirm.required}")
    private String confirmPassword;

    @NotBlank(message = "{validation.user.firstname.required}")
    @Size(min = 2, max = 30, message = "{validation.user.firstname.size}")
    @Pattern(regexp = "^[A-Za-z]+([ '-][A-Za-z]+)*$", message = "{validation.user.firstname.pattern}")
    private String firstName;

    @NotBlank(message = "{validation.user.lastname.required}")
    @Size(min = 2, max = 30, message = "{validation.user.lastname.size}")
    @Pattern(regexp = "^[A-Za-z]+([ '-][A-Za-z]+)*$", message = "{validation.user.lastname.pattern}")
    private String lastName;

    @Size(min = 2, max = 30, message = "{validation.user.middlename.size}")
    @Pattern(regexp = "^[A-Za-z]+([ '-][A-Za-z]+)*$", message = "{validation.user.middlename.pattern}")
    private String middleName;
}