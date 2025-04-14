package com.shopsavvy.shopshavvy.dto.loginDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponseDTO {

    private String accessToken;
    private String refreshToken;
    private String email;
    private Set<String> role;
}
