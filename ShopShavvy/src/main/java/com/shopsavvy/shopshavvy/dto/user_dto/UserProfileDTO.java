package com.shopsavvy.shopshavvy.dto.user_dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.shopsavvy.shopshavvy.dto.address_dto.AddressDTO;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;

    @Size(min = 2, max = 30, message = "{validation.user.firstname.size}", groups = OnUpdate.class)
    @Pattern(regexp = "^[A-Za-z]+([ '-][A-Za-z]+)*$",
            message = "{validation.user.firstname.pattern}",
            groups = OnUpdate.class)
    private String firstName;

    @Size(min = 2, max = 30, message = "{validation.user.middlename.size}", groups = OnUpdate.class)
    @Pattern(regexp = "^[A-Za-z]+([ '-][A-Za-z]+)*$",
            message = "{validation.user.middlename.pattern}",
            groups = OnUpdate.class)
    private String middleName;

    @Size(min = 2, max = 30, message = "{validation.user.lastname.size}", groups = OnUpdate.class)
    @Pattern(regexp = "^[A-Za-z]+([ '-][A-Za-z]+)*$",
            message = "{validation.user.lastname.pattern}",
            groups = OnUpdate.class)
    private String lastName;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean active;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String imageUrl;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private MultipartFile profileImage;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Set<AddressDTO> addresses;
}