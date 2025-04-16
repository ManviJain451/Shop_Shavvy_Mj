package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.dto.passwordDto.PasswordDTO;
import com.shopsavvy.shopshavvy.exception.UserNotFoundException;
import com.shopsavvy.shopshavvy.model.users.User;
import com.shopsavvy.shopshavvy.repository.UserRepository;
import com.shopsavvy.shopshavvy.security.configurations.UserDetailsImpl;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
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
public class UserService {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final MessageSource messageSource;

    private Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }


    public String updatePassword(UserDetailsImpl userDetailsImpl, PasswordDTO passwordUpdateDTO) throws MessagingException, BadRequestException {
        if (!passwordUpdateDTO.getPassword().equals(passwordUpdateDTO.getConfirmPassword())) {
            throw new BadRequestException(messageSource.getMessage("error.passwordMismatch", null, getCurrentLocale()));
        }


        User user = userRepository.findByEmail(userDetailsImpl.getUsername())
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.seller.not.found.token", null, getCurrentLocale())));

        user.setPassword(passwordEncoder.encode(passwordUpdateDTO.getPassword()));
        userRepository.save(user);

        emailService.sendPasswordChangeNotification(userDetailsImpl.getUsername());
        return messageSource.getMessage("success.password.updated", null, getCurrentLocale());
    }
}
