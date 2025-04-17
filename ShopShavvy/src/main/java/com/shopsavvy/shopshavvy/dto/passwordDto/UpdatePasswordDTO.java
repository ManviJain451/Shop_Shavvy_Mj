package com.shopsavvy.shopshavvy.dto.passwordDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePasswordDTO extends PasswordDTO{
    private String oldPassword;
}
