package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.dto.password_dto.UpdatePasswordDTO;
import com.shopsavvy.shopshavvy.exception.UserNotFoundException;
import com.shopsavvy.shopshavvy.model.users.User;
import com.shopsavvy.shopshavvy.repository.UserRepository;
import com.shopsavvy.shopshavvy.configuration.UserDetailsImpl;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final MessageSource messageSource;

    private Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }


    public String updatePassword(UserDetailsImpl userDetailsImpl, UpdatePasswordDTO updatePasswordDTO) throws MessagingException, BadRequestException {
        log.info("Attempting password update for user: {}", userDetailsImpl.getUsername());
        User user = userRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.seller.not.found.token", null, getCurrentLocale())));

        if (!passwordEncoder.matches(updatePasswordDTO.getOldPassword(), user.getPassword())) {
            log.warn("Old password mismatch for user: {}", user.getEmail());
            throw new BadRequestException(messageSource.getMessage("error.oldPassword.mismatch", null, getCurrentLocale()));
        }

        if (!updatePasswordDTO.getPassword().equals(updatePasswordDTO.getConfirmPassword())) {
            throw new BadRequestException(messageSource.getMessage("error.passwordMismatch", null, getCurrentLocale()));
        }

        user.setPassword(passwordEncoder.encode(updatePasswordDTO.getPassword()));
        userRepository.save(user);

        log.info("Password successfully updated for user: {}", user.getEmail());
        emailService.sendPasswordChangeNotification(userDetailsImpl.getUsername());
        return messageSource.getMessage("success.password.updated", null, getCurrentLocale());
    }
}
