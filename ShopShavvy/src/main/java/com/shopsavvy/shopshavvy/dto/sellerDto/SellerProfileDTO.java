package com.shopsavvy.shopshavvy.dto.sellerDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.shopsavvy.shopshavvy.dto.addressDto.AddressDTO;
import com.shopsavvy.shopshavvy.dto.userDto.UserProfileDTO;
import com.shopsavvy.shopshavvy.model.users.Address;
import com.shopsavvy.shopshavvy.validation.groups.OnCreate;
import com.shopsavvy.shopshavvy.validation.groups.OnUpdate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class SellerProfileDTO extends UserProfileDTO {

    @Pattern(regexp = "^[0-9]{10}$",
            message = "Contact must be exactly 10 digits",
            groups = OnUpdate.class)
    private String companyContact;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Size(min = 3, max = 100, message = "Company name must be between 3 and 100 characters", groups = OnUpdate.class)
    private String companyName;

    @Pattern(groups = OnUpdate.class, regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$", message = "GST should be valid as per Govt. norms")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String gst;

}
