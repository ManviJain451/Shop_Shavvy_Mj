package com.shopsavvy.shopshavvy.dto.passwordDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordResponseDTO extends PasswordDTO {
    private String resetPasswordToken;

    public ResetPasswordResponseDTO(String resetPasswordToken, String password, String confirmPassword) {
        super();
        this.resetPasswordToken = resetPasswordToken;
    }
}

