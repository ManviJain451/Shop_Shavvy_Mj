package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.dto.PasswordDTO;
import com.shopsavvy.shopshavvy.exception.UserNotFoundException;
import com.shopsavvy.shopshavvy.model.users.User;
import com.shopsavvy.shopshavvy.repository.UserRepository;
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

    public void updatePassword(String accessToken, PasswordDTO passwordUpdateDTO) throws MessagingException {
        if (!passwordUpdateDTO.getPassword().equals(passwordUpdateDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and confirm password do not match.");
        }

        String email = jwtService.extractUsername(accessToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Seller not found for the provided access token."));

        user.setPassword(passwordEncoder.encode(passwordUpdateDTO.getPassword()));
        userRepository.save(user);

        emailService.sendPasswordChangeNotification(email);
    }
}
