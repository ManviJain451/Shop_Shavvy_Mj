package com.shopsavvy.shopshavvy.dto.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.shopsavvy.shopshavvy.dto.user.UserProfileDTO;
import com.shopsavvy.shopshavvy.validation.groups.OnUpdate;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class SellerProfileDTO extends UserProfileDTO {

    @Pattern(regexp = "^[0-9]{10}$",
            message = "{validation.seller.contact.pattern}",
            groups = OnUpdate.class)
    private String companyContact;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Size(min = 3, max = 255,
            message = "{validation.seller.company.name.size}",
            groups = OnUpdate.class)
    private String companyName;

    @Pattern(groups = OnUpdate.class,
            regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$",
            message = "{validation.seller.gst.pattern}")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String gst;
}