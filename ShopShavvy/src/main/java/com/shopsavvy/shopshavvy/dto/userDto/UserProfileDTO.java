package com.shopsavvy.shopshavvy.dto.userDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.shopsavvy.shopshavvy.dto.addressDto.AddressDTO;
import com.shopsavvy.shopshavvy.validation.groups.OnUpdate;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UserProfileDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;

    @Size(min = 2, max = 30, message = "First name must be between 2 and 30 characters", groups = OnUpdate.class)
    @Pattern(regexp = "^[A-Za-z]+([ '-][A-Za-z]+)*$",
            message = "First name can only contain letters, spaces, hyphens, and apostrophes",
            groups = OnUpdate.class)
    private String firstName;

    @Size(min = 2, max = 30, message = "Middle name must be between 2 and 30 characters", groups = OnUpdate.class)
    @Pattern(regexp = "^[A-Za-z]+([ '-][A-Za-z]+)*$",
            message = "Middle name can only contain letters, spaces, hyphens, and apostrophes",
            groups = OnUpdate.class)
    private String middleName;

    @Size(min = 2, max = 30, message = "Last name must be between 2 and 30 characters", groups = OnUpdate.class)
    @Pattern(regexp = "^[A-Za-z]+([ '-][A-Za-z]+)*$",
            message = "Last name can only contain letters, spaces, hyphens, and apostrophes",
            groups = OnUpdate.class)
    private String lastName;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean isActive;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String imageUrl;

    private MultipartFile profileImage;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private Set<AddressDTO> addresses;
}
