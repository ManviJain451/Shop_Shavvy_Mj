package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.dto.passwordDto.PasswordDTO;
import com.shopsavvy.shopshavvy.exception.UserNotFoundException;
import com.shopsavvy.shopshavvy.model.users.User;
import com.shopsavvy.shopshavvy.repository.UserRepository;
import com.shopsavvy.shopshavvy.security.configurations.UserDetailsImpl;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public void updatePassword(UserDetailsImpl userDetailsImpl, PasswordDTO passwordUpdateDTO) throws MessagingException {
        if (!passwordUpdateDTO.getPassword().equals(passwordUpdateDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and confirm password do not match.");
        }


        User user = userRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Seller not found for the provided access token."));

        user.setPassword(passwordEncoder.encode(passwordUpdateDTO.getPassword()));
        userRepository.save(user);

        emailService.sendPasswordChangeNotification(userDetailsImpl.getUsername());
    }
}
