package com.shopsavvy.shopshavvy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class CustomerUpdateProfileDTO {

    @Size(max = 15, message = "First name must not exceed 15 characters.")
    private String firstName;

    @Size(max = 15, message = "Middle name must not exceed 15 characters.")
    private String middleName;

    @Size(max = 15, message = "Last name must not exceed 15 characters.")
    private String lastName;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid phone number. Must be a 10-digit Indian number starting with 6-9")
    private String contact;

    private MultipartFile profileImage;
}