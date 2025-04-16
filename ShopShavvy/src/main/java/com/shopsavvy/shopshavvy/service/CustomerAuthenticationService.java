package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.dto.EmailDTO;
import com.shopsavvy.shopshavvy.exception.*;
import com.shopsavvy.shopshavvy.dto.customerDto.CustomerRegistrationDTO;
import com.shopsavvy.shopshavvy.model.token.AuthToken;
import com.shopsavvy.shopshavvy.model.users.*;
import com.shopsavvy.shopshavvy.repository.BlackListedTokenRepository;
import com.shopsavvy.shopshavvy.repository.AuthTokenRepository;
import com.shopsavvy.shopshavvy.repository.RoleRepository;
import com.shopsavvy.shopshavvy.repository.UserRepository;
import com.shopsavvy.shopshavvy.security.configurations.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.management.relation.RoleNotFoundException;
import java.util.Set;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerAuthenticationService {

    @Value("${jwt.expiration-time.accessToken}")
    private long accessTokenExpirationTime;

    @Value("${jwt.expiration-time.refreshToken}")
    private long refreshTokenExpirationTime;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final AuthTokenRepository authTokenRepository;
    private final AuthenticationService authenticationService;
    private final RoleRepository roleRepository;
    private final BlackListedTokenRepository blackListedTokenRepository;
    private final BlackListedTokenService blackListedTokenService;
    private final MessageSource messageSource;

    private Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    public String registerCustomer(CustomerRegistrationDTO customerRegistrationDTO) throws Exception {

        if(userRepository.existsByEmail(customerRegistrationDTO.getEmail())){
            throw new EmailAlreadyExistsException(messageSource.getMessage("error.emailExists", null, getCurrentLocale()));
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

        if (!customerRegistrationDTO.getConfirmPassword().equals(customerRegistrationDTO.getPassword())) {
            throw new PasswordMismatchException(messageSource.getMessage("error.passwordMismatch", null, getCurrentLocale()));
        }

        Role role = roleRepository.findByAuthority("ROLE_CUSTOMER");
        customer.addRole(role);

        userRepository.save(customer);

        UserDetailsImpl userDetails = new UserDetailsImpl(customer);
        String token = jwtService.generateToken(userDetails, "activation");
        Claims claims = jwtService.extractAllClaims(token);

        AuthToken authToken = AuthToken.builder()
                .userEmail(customer.getEmail())
                .token(token)
                .tokenType("activation")
                .expirationTime(claims.getExpiration())
                .build();
        authTokenRepository.save(authToken);

        try {
            emailService.sendActivationLink(customerRegistrationDTO.getEmail(), token);
        } catch (Exception e) {
            throw new Exception(messageSource.getMessage("error.activationEmailNotSent", null, getCurrentLocale()));
        }

        return messageSource.getMessage("success.customerRegistered", null, getCurrentLocale());
    }

    @Transactional
    public String activateCustomer(String token) throws Exception {
        String userEmail = jwtService.extractUsername(token);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.userNotFound",null, getCurrentLocale())));

        if (user.getIsActive()) {
            throw new AlreadyActivatedException(messageSource.getMessage("error.userAlreadyActivated", null, getCurrentLocale()));
        }

        UserDetailsImpl userDetailsImpl = new UserDetailsImpl(user);

        if (jwtService.isTokenValid(token, userDetailsImpl, "activation")) {
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
            throw new Exception(messageSource.getMessage("error.verification.email.not.sent", null, locale));
        }
    }

    @Transactional
    public String resendActivationLink(EmailDTO emailDTO) throws Exception {
        String email = emailDTO.getEmail();
        if (!userRepository.existsByEmail(email)) {
            throw new UserNotFoundException(messageSource.getMessage("error.userNotFound", null, getCurrentLocale()));
        }

        Role role = roleRepository.findByAuthority("ROLE_CUSTOMER");
        if (role == null) {
            throw new RoleNotFoundException(messageSource.getMessage("error.roleNotFound", null, getCurrentLocale()));
        }

        User user = userRepository.findByEmailAndRoles(email, Set.of(role))
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.customerNotFound", null, getCurrentLocale())));

        if (user.getIsActive()) {
            throw new AlreadyActivatedException(messageSource.getMessage("error.userAlreadyActivated", null, getCurrentLocale()));
        }

        authTokenRepository.deleteActivationTokenByEmail(email);

        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        String token = jwtService.generateToken(userDetails, "activation");

        Claims claims = jwtService.extractAllClaims(token);

        AuthToken authToken = AuthToken.builder()
                .userEmail(email)
                .token(token)
                .tokenType("activation")
                .expirationTime(claims.getExpiration())
                .build();
        authTokenRepository.save(authToken);

        try {
            emailService.sendActivationLink(email, token);
        } catch (Exception e) {
            throw new Exception(messageSource.getMessage("error.activationEmailNotSent", null, getCurrentLocale()));
        }

        return messageSource.getMessage("success.activationLinkSent", null, getCurrentLocale());
    }
}
