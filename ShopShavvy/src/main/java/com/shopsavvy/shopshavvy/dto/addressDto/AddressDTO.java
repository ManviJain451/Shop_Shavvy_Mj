package com.shopsavvy.shopshavvy.dto.addressDto;

import com.shopsavvy.shopshavvy.validation.groups.OnCreate;
import com.shopsavvy.shopshavvy.validation.groups.OnUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {

    @NotBlank(groups = OnCreate.class, message = "city is required")
    @Size(min = 2, max = 50, message = "City name must be between 2 to 50 characters.", groups = {OnCreate.class, OnUpdate.class})
    private String city;

    @NotBlank(groups = OnCreate.class, message = "State is required.")
    @Size(min = 2, max = 50, message = "State name must be between 2 to 50 characters.", groups = {OnCreate.class, OnUpdate.class})
    private String state;

    @NotBlank(groups = OnCreate.class, message = "Country is required.")
    @Size(min = 4, max = 50, message = "Country name must be between 4 to 50 characters.", groups = {OnCreate.class, OnUpdate.class})
    private String country;

    @NotBlank(groups = OnCreate.class, message = "Address line is required.")
    @Size(min = 2, max = 255, message = "Address line must be between 2 to 255 characters.", groups = {OnCreate.class, OnUpdate.class})
    private String addressLine;

    @NotBlank(groups = OnCreate.class, message = "Zip code is required.")
    @Pattern(regexp = "\\d{5,6}", message = "ZIP code must be 5 or 6 digits", groups = {OnCreate.class, OnUpdate.class})
    private String zipCode;

    @Size(max = 15, message = "Label must not exceed 15 characters.", groups = {OnCreate.class, OnUpdate.class})
    private String label;

}


