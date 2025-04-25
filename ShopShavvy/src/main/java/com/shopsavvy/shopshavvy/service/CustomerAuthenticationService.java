package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.dto.EmailDTO;
import com.shopsavvy.shopshavvy.exception.*;
import com.shopsavvy.shopshavvy.dto.customer_dto.CustomerRegistrationDTO;
import com.shopsavvy.shopshavvy.model.token.AuthToken;
import com.shopsavvy.shopshavvy.model.users.*;
import com.shopsavvy.shopshavvy.repository.AuthTokenRepository;
import com.shopsavvy.shopshavvy.repository.RoleRepository;
import com.shopsavvy.shopshavvy.repository.UserRepository;
import com.shopsavvy.shopshavvy.configuration.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import jakarta.mail.SendFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.management.relation.RoleNotFoundException;
import java.util.Set;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CustomerAuthenticationService {

    @Value("${jwt.expiration-time.accessToken}")
    private long accessTokenExpirationTime;

    @Value("${jwt.expiration-time.refreshToken}")
    private long refreshTokenExpirationTime;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final AuthTokenRepository authTokenRepository;
    private final RoleRepository roleRepository;
    private final MessageSource messageSource;

    private Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    private static final String ACTIVATION = "activation";

    public String registerCustomer(CustomerRegistrationDTO customerRegistrationDTO) throws Exception {
        log.info("Registering customer: {}", customerRegistrationDTO.getEmail());

        if(userRepository.existsByEmail(customerRegistrationDTO.getEmail())){
            log.warn("Email already exists: {}", customerRegistrationDTO.getEmail());
            throw new DuplicateEntryExistsException(messageSource.getMessage("error.emailExists", null, getCurrentLocale()));
        }

        if (!customerRegistrationDTO.getConfirmPassword().equals(customerRegistrationDTO.getPassword())) {
            log.warn("Password mismatch for: {}", customerRegistrationDTO.getEmail());
            throw new PasswordMismatchException(messageSource.getMessage("error.passwordMismatch", null, getCurrentLocale()));
        }

        Customer customer = Customer.builder()
                .email(customerRegistrationDTO.getEmail())
                .firstName(customerRegistrationDTO.getFirstName())
                .lastName(customerRegistrationDTO.getLastName())
                .password(passwordEncoder.encode(customerRegistrationDTO.getPassword()))
                .contact(customerRegistrationDTO.getContact())
                .middleName((customerRegistrationDTO.getMiddleName() != null && !customerRegistrationDTO.getMiddleName().isBlank()) ? customerRegistrationDTO.getMiddleName() : null)
                .isActive(false)
                .isDeleted(false)
                .build();

        Role role = roleRepository.findByAuthority("ROLE_CUSTOMER");
        customer.addRole(role);

        userRepository.save(customer);

        UserDetailsImpl userDetails = new UserDetailsImpl(customer);
        String token = jwtService.generateToken(userDetails, ACTIVATION);
        Claims claims = jwtService.extractAllClaims(token);

        AuthToken authToken = AuthToken.builder()
                .userEmail(customer.getEmail())
                .token(token)
                .tokenType(ACTIVATION)
                .expirationTime(claims.getExpiration())
                .build();
        authTokenRepository.save(authToken);

        try {
            emailService.sendActivationLink(customerRegistrationDTO.getEmail(), token);
        } catch (Exception e) {
            log.error("Failed to send activation email: {}", e.getMessage());
            throw new SendFailedException(messageSource.getMessage("error.activationEmailNotSent", null, getCurrentLocale()));
        }

        return messageSource.getMessage("success.customerRegistered", null, getCurrentLocale());
    }

    public String activateCustomer(String token) throws Exception {
        String userEmail = jwtService.extractUsername(token);
        log.info("Activating customer: {}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", userEmail);
                    return new UserNotFoundException(messageSource.getMessage("error.userNotFound",null, getCurrentLocale()));
                });

        if (Boolean.TRUE.equals(user.getIsActive())) {
            log.warn("User already activated: {}", userEmail);
            throw new AlreadyActivatedException(messageSource.getMessage("error.userAlreadyActivated", null, getCurrentLocale()));
        }

        UserDetailsImpl userDetailsImpl = new UserDetailsImpl(user);

        if (jwtService.isTokenValid(token, userDetailsImpl, ACTIVATION)) {
            user.setIsActive(true);
            userRepository.save(user);
            verifyCustomer(user.getEmail(), getCurrentLocale());
        }
        return messageSource.getMessage("success.userActivated", null, getCurrentLocale());
    }

    public void verifyCustomer(String email, Locale locale) throws Exception {
        try {
            emailService.sendVerificationEmail(email,
                    "Account Activated",
                    "Your account has been successfully activated.");
        } catch (Exception e) {
            log.error("Failed to send verification email: {}", e.getMessage());
            throw new SendFailedException(messageSource.getMessage("error.verification.email.not.sent", null, locale));
        }
    }

    public String resendActivationLink(EmailDTO emailDTO) throws Exception {
        String email = emailDTO.getEmail();
        log.info("Resending activation link: {}", email);

        if (!userRepository.existsByEmail(email)) {
            log.warn("User not found: {}", email);
            throw new UserNotFoundException(messageSource.getMessage("error.userNotFound", null, getCurrentLocale()));
        }

        Role role = roleRepository.findByAuthority("ROLE_CUSTOMER");
        if (role == null) {
            log.error("ROLE_CUSTOMER not found");
            throw new RoleNotFoundException(messageSource.getMessage("error.roleNotFound", null, getCurrentLocale()));
        }

        User user = userRepository.findByEmailAndRoles(email, Set.of(role))
                .orElseThrow(() -> {
                    log.warn("Customer not found: {}", email);
                    return new UserNotFoundException(messageSource.getMessage("error.customerNotFound", null, getCurrentLocale()));
                });

        if (Boolean.TRUE.equals(user.getIsActive())) {
            log.warn("User already activated: {}", email);
            throw new AlreadyActivatedException(messageSource.getMessage("error.userAlreadyActivated", null, getCurrentLocale()));
        }

        authTokenRepository.deleteActivationTokenByEmail(email);

        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        String token = jwtService.generateToken(userDetails, ACTIVATION);
        Claims claims = jwtService.extractAllClaims(token);

        AuthToken authToken = AuthToken.builder()
                .userEmail(email)
                .token(token)
                .tokenType(ACTIVATION)
                .expirationTime(claims.getExpiration())
                .build();
        authTokenRepository.save(authToken);

        try {
            emailService.sendActivationLink(email, token);
        } catch (Exception e) {
            log.error("Failed to send activation email: {}", e.getMessage());
            throw new SendFailedException(messageSource.getMessage("error.activationEmailNotSent", null, getCurrentLocale()));
        }

        return messageSource.getMessage("success.activationLinkSent", null, getCurrentLocale());
    }
}