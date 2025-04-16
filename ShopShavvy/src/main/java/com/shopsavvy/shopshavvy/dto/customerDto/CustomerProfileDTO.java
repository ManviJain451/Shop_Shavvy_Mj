package com.shopsavvy.shopshavvy.dto.customerDto;

import com.shopsavvy.shopshavvy.dto.userDto.UserProfileDTO;
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