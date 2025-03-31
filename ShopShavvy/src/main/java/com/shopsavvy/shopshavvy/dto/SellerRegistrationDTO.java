package com.shopsavvy.shopshavvy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellerRegistrationDTO extends UserRegistrationDTO {

    @NotBlank(message = "GST is mandatory")
    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$", message = "GST should be valid as per Govt. norms")
    private String gst;

    @NotBlank(message = "Company name is mandatory")
    private String companyName;

    @NotBlank(message = "Company contact is mandatory")
    private String companyContact;

    @NotBlank(message = "Company address is mandatory")
    private String companyAddress;
}
