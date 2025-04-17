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

    @NotBlank(groups = OnCreate.class, message = "{validation.address.city.required}")
    @Size(min = 2, max = 50, message = "{validation.address.city.size}", groups = {OnCreate.class, OnUpdate.class})
    @Pattern(regexp = "^[a-zA-Z\\s\\-'.]+$", message = "{validation.address.pattern}", groups = {OnCreate.class, OnUpdate.class})
    private String city;

    @NotBlank(groups = OnCreate.class, message = "{validation.address.state.required}")
    @Size(min = 2, max = 50, message = "{validation.address.state.size}", groups = {OnCreate.class, OnUpdate.class})
    @Pattern(regexp = "^[a-zA-Z\\s\\-'.]+$", message = "{validation.address.pattern}", groups = {OnCreate.class, OnUpdate.class})
    private String state;

    @NotBlank(groups = OnCreate.class, message = "{validation.address.country.required}")
    @Size(min = 4, max = 50, message = "{validation.address.country.size}", groups = {OnCreate.class, OnUpdate.class})
    @Pattern(regexp = "^[a-zA-Z\\s\\-'.]+$", message = "{validation.address.pattern}", groups = {OnCreate.class, OnUpdate.class})
    private String country;

    @NotBlank(groups = OnCreate.class, message = "{validation.address.line.required}")
    @Size(min = 2, max = 255, message = "{validation.address.line.size}", groups = {OnCreate.class, OnUpdate.class})
    private String addressLine;

    @NotBlank(groups = OnCreate.class, message = "{validation.address.zipcode.required}")
    @Pattern(regexp = "\\d{5,6}", message = "{validation.address.zipcode.pattern}", groups = {OnCreate.class, OnUpdate.class})
    private String zipCode;

    @Size(max = 15, message = "{validation.address.label.size}", groups = {OnCreate.class, OnUpdate.class})
    private String label;
}


