package com.shopsavvy.shopshavvy.dto.seller;

import com.shopsavvy.shopshavvy.dto.user.UserRegistrationDTO;
import com.shopsavvy.shopshavvy.dto.address.AddressDTO;
import com.shopsavvy.shopshavvy.validation.groups.OnCreate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.ConvertGroup;
import jakarta.validation.groups.Default;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellerRegistrationDTO extends UserRegistrationDTO {

    @NotBlank(message = "{validation.seller.gst.required}")
    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$",
            message = "{validation.seller.gst.pattern}")
    private String gst;

    @NotBlank(message = "{validation.seller.company.name.required}")
    @Size(min = 3, max = 255, message = "{validation.seller.company.name.size}")
    private String companyName;

    @Pattern(regexp = "^[0-9]{10}$",
            message = "{validation.seller.contact.pattern}")
    private String companyContact;

    @Valid
    @ConvertGroup(from = Default.class, to = OnCreate.class)
    private AddressDTO address;
}