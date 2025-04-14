package com.shopsavvy.shopshavvy.dto.sellerDto;

import com.shopsavvy.shopshavvy.dto.userDto.UserRegistrationDTO;
import com.shopsavvy.shopshavvy.dto.addressDto.AddressDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellerRegistrationDTO extends UserRegistrationDTO {

    @NotBlank(message = "GST is mandatory")
    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$", message = "GST should be valid as per Govt. norms")
    private String gst;

    @NotBlank(message = "Company name is mandatory")
    @Size(min = 3, max = 100, message = "Company name must be between 3 and 100 characters")
    private String companyName;

    @Pattern(regexp = "^[0-9]{10}$",
            message = "Contact must be exactly 10 digits")
    private String companyContact;

    @Valid
    private AddressDTO address;
}
