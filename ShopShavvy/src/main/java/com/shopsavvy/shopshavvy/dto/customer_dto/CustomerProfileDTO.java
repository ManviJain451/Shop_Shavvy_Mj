package com.shopsavvy.shopshavvy.dto.customer_dto;

import com.shopsavvy.shopshavvy.dto.user_dto.UserProfileDTO;
import com.shopsavvy.shopshavvy.validation.groups.OnUpdate;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class CustomerProfileDTO extends UserProfileDTO {

    @Pattern(regexp = "^[0-9]{10}$",
            message = "{validation.customer.contact.pattern}",
            groups = OnUpdate.class)
    private String contact;
}