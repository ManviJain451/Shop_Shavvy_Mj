package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.dto.user_dto.UserRegistrationDTO;
import com.shopsavvy.shopshavvy.exception.*;
import com.shopsavvy.shopshavvy.dto.login_dto.LoginResponseDTO;
import com.shopsavvy.shopshavvy.dto.login_dto.LoginRequestDTO;
import com.shopsavvy.shopshavvy.model.token.AuthToken;
import com.shopsavvy.shopshavvy.model.users.Role;
import com.shopsavvy.shopshavvy.model.users.User;
import com.shopsavvy.shopshavvy.repository.BlackListedTokenRepository;
import com.shopsavvy.shopshavvy.repository.AuthTokenRepository;
import com.shopsavvy.shopshavvy.repository.RoleRepository;
import com.shopsavvy.shopshavvy.repository.UserRepository;
import com.shopsavvy.shopshavvy.configuration.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import jakarta.mail.MessagingException;
import jakarta.mail.SendFailedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    @Value("${max.attempt.wrong.credentials}")
    private int maxInvalidAttempts;

    @Value("${password.validity.in.months}")
    private long passwordValidity;

    @Value("${jwt.expiration-time.accessToken}")
    private long accessTokenExpirationTime;

    @Value("${jwt.expiration-time.refreshToken}")
    private long refreshTokenExpirationTime;

    private final UserRepository userRepository;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final JwtService jwtService;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RoleRepository roleRepository;
    private final BlackListedTokenRepository blackListedTokenRepository;
    private final BlackListedTokenService blackListedTokenService;
    private final MessageSource messageSource;

    private Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    @Transactional
    public String registerAdmin(UserRegistrationDTO userRegistrationDTO) throws MessagingException {
        log.info("Registering new admin with email: {}", userRegistrationDTO.getEmail());

        if(userRepository.existsByEmail(userRegistrationDTO.getEmail())){
            log.warn("Email already exists: {}", userRegistrationDTO.getEmail());
            throw new DuplicateEntryExistsException(messageSource.getMessage("error.emailExists", null, getCurrentLocale()));
        }

        if (!userRegistrationDTO.getConfirmPassword().equals(userRegistrationDTO.getPassword())) {
            log.warn("Password mismatch for: {}", userRegistrationDTO.getEmail());
            throw new PasswordMismatchException(messageSource.getMessage("error.passwordMismatch", null, getCurrentLocale()));
        }

        User adminUser = User.builder()
                .email(userRegistrationDTO.getEmail())
                .firstName(userRegistrationDTO.getFirstName())
                .lastName(userRegistrationDTO.getLastName())
                .middleName((userRegistrationDTO.getMiddleName() != null && !userRegistrationDTO.getMiddleName().isBlank()) ? userRegistrationDTO.getMiddleName() : null)
                .password(passwordEncoder.encode(userRegistrationDTO.getPassword()))
                .isActive(true)
                .isDeleted(false)
                .build();

        Role role = roleRepository.findByAuthority("ROLE_ADMIN");
        adminUser.addRole(role);

        userRepository.save(adminUser);
        try {
            emailService.sendVerificationEmail(
                    adminUser.getEmail(),
                    "Admin Account Created",
                    "Admin account has been created. Admin account has been activated."
            );
            log.debug("Verification email sent to admin: {}", adminUser.getEmail());
        } catch (SendFailedException e) {
            throw e;
        }
        return messageSource.getMessage("admin.register.success", null, getCurrentLocale());
    }

    public LoginResponseDTO authenticate(LoginRequestDTO loginRequestDTO, HttpServletResponse httpServletResponse, String expectedRole) throws InvalidRoleException, MessagingException, BadRequestException {
        log.info("Authentication attempt for user: {}", loginRequestDTO.getEmail());
        User user = userRepository.findByEmail(loginRequestDTO.getEmail())
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("user.not.found", null, getCurrentLocale())));

        boolean hasExpectedRole = user.getRoles().stream()
                .anyMatch(role -> role.getAuthority().equals(expectedRole));
        if (!hasExpectedRole) {
            throw new InvalidRoleException(messageSource.getMessage("error.invalid.role", new Object[]{expectedRole}, getCurrentLocale()));
        }

        if (user.isLocked()) {
            throw new LockedException(messageSource.getMessage("account.locked", null, getCurrentLocale()));
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new BadRequestException(messageSource.getMessage("account.not.activated", null, getCurrentLocale()));
        }

        if(Boolean.TRUE.equals(user.getIsDeleted())){
            throw new UserNotFoundException(messageSource.getMessage("user.not.found", null, getCurrentLocale()));
        }

        if (isPasswordCredentialExpired(loginRequestDTO.getEmail())) {
            throw new BadCredentialsException(messageSource.getMessage("password.expired", null, getCurrentLocale()));
        }

        if (!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
            user.setInvalidAttemptCount(user.getInvalidAttemptCount() + 1);
            log.warn("Authentication failed: Invalid password attempt {} of {} for user: {}",
                    user.getInvalidAttemptCount(), maxInvalidAttempts, loginRequestDTO.getEmail());

            if (user.getInvalidAttemptCount() >= maxInvalidAttempts) {
                user.setLocked(true);
                userRepository.save(user);
                log.warn("Account locked due to maximum invalid attempts for user: {}", loginRequestDTO.getEmail());

                try {
                    emailService.sendVerificationEmail(loginRequestDTO.getEmail(),
                            "Your ShopShavvy Account has been locked",
                            "Your account has been locked. Please contact support.");
                } catch (MessagingException e) {
                    throw e;
                }

                throw new LockedException(messageSource.getMessage("account.locked", null, getCurrentLocale()));
            }
            userRepository.save(user);
            throw new BadCredentialsException(messageSource.getMessage("invalid.credentials", null, getCurrentLocale()));
        }

        user.setInvalidAttemptCount(0);
        userRepository.save(user);

        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        String accessToken = jwtService.generateToken(userDetails, "access");
        String refreshToken = jwtService.generateToken(userDetails, "refresh");

        ResponseCookie accessTokencookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(accessTokenExpirationTime)
                .build();

        ResponseCookie refreshTokencookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(refreshTokenExpirationTime)
                .build();

        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, accessTokencookie.toString());
        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, refreshTokencookie.toString());

        Set<String> roles = user.getRoles().stream()
                .map(Role::getAuthority)
                .collect(Collectors.toSet());

        log.info("Authentication successful for user: {} with roles: {}", loginRequestDTO.getEmail(), roles);
        return new LoginResponseDTO(accessToken, refreshToken, loginRequestDTO.getEmail(), roles);
    }


    public boolean isPasswordCredentialExpired(String email) {
        log.debug("Checking password expiration for user: {}", email);
        LocalDateTime passwordLastUpdateDate = userRepository.findPasswordUpdateDateByEmail(email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("user.not.found", null, getCurrentLocale())));

        if (passwordLastUpdateDate.isBefore(LocalDateTime.now().minus(passwordValidity, ChronoUnit.MONTHS))) {
            user.setExpired(true);
            userRepository.save(user);
            return true;
        }
        log.debug("Password is not expired for user: {}", email);
        return false;
    }

    @Transactional
    public String userLogout(String accessToken, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws MessagingException {
        log.info("Processing logout request");
        if (blackListedTokenRepository.existsByToken(accessToken)) {
            throw new TokenNotFoundException(messageSource.getMessage("access.token.not.found", null, getCurrentLocale()));
        }

        String userEmail = jwtService.extractUsername(accessToken);

        UserDetailsImpl userDetailsImpl = userDetailsServiceImpl.loadUserByUsername(userEmail);
        if (!jwtService.isTokenValid(accessToken, userDetailsImpl, "access")) {
            throw new InvalidTokenException(messageSource.getMessage("access.token.expired", null, getCurrentLocale()));
        }

        blackListedTokenService.blacklistAccessToken(httpServletRequest);
        blackListedTokenService.blacklistRefreshToken(httpServletRequest);

        ResponseCookie accessTokencookie = ResponseCookie.from("accessToken", null)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie refreshTokencookie = ResponseCookie.from("refreshToken", null)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .build();

        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, accessTokencookie.toString());
        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, refreshTokencookie.toString());

        log.info("User logout successful: {}", userEmail);
        return messageSource.getMessage("user.logout.success", null, getCurrentLocale());
    }

    @Transactional
    public String refreshToken(String refreshToken, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws RuntimeException, MessagingException {
        log.info("Processing token refresh request");
        String userEmail = jwtService.extractUsername(refreshToken);

        UserDetailsImpl userDetailsImpl = userDetailsServiceImpl.loadUserByUsername(userEmail);
        jwtService.isTokenValid(refreshToken, userDetailsImpl, "refresh");

        blackListedTokenService.blacklistAccessToken(httpServletRequest);

        String newAccessToken = jwtService.generateToken(userDetailsImpl, "access");

        ResponseCookie accessTokencookie = ResponseCookie.from("accessToken", newAccessToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(accessTokenExpirationTime)
                .build();

        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, accessTokencookie.toString());

        log.info("Token refresh successful for user: {}", userEmail);
        return newAccessToken;
    }

    @Transactional
    public String forgotPassword(String email) throws MessagingException {
        log.info("Processing forgot password request for email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("user.not.found", null, getCurrentLocale())));


        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new AlreadyDeactivatedException(messageSource.getMessage("account.not.activated", null, getCurrentLocale()));
        }

        UserDetailsImpl userDetailsImpl = new UserDetailsImpl(user);
        authTokenRepository.deleteResetPasswordTokenByEmail(email);

        String resetPasswordToken = jwtService.generateToken(userDetailsImpl, "reset_password");
        Claims claimsForResetPasswordToken = jwtService.extractAllClaims(resetPasswordToken);

        AuthToken resetPasswordAuthToken = AuthToken.builder()
                .userEmail(email)
                .token(resetPasswordToken)
                .tokenType("reset_password")
                .expirationTime(claimsForResetPasswordToken.getExpiration())
                .build();

        authTokenRepository.save(resetPasswordAuthToken);
        log.debug("Saved reset password token to database for user: {}", email);

        try {
            emailService.sendVerificationEmail(email,
                    "Password Reset Request",
                    "To reset your password, click the link below:\n" + "http://localhost:8080/api/v1/auth/reset-password?token=" + resetPasswordToken);
        } catch (MessagingException e) {
            log.error("Failed to send reset password email to user: {}", email, e);
            throw e;
        }

        log.info("Forgot password process completed successfully for user: {}", email);
        return messageSource.getMessage("password.reset.link.sent", null, getCurrentLocale());
    }

    @Transactional
    public String resetPassword(String resetPasswordtoken, String password, String confirmPassword) throws MessagingException {
        log.info("Processing reset password request");
        String userEmail = jwtService.extractUsername(resetPasswordtoken);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.warn("Reset password failed: User not found with email: {}", userEmail);
                    return new UserNotFoundException(messageSource.getMessage("user.not.found", null, getCurrentLocale()));
                });

        UserDetailsImpl userDetailsImpl = new UserDetailsImpl(user);
        jwtService.isTokenValid(resetPasswordtoken, userDetailsImpl, "reset_password");

        if (!password.equals(confirmPassword)) {
            throw new PasswordMismatchException(messageSource.getMessage("password.mismatch", null, getCurrentLocale()));
        }

        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);
        user.setExpired(false);
        userRepository.save(user);

        authTokenRepository.deleteByToken(resetPasswordtoken);

        try {
            emailService.sendVerificationEmail(user.getEmail(), "Password Reset Successful", "Your password has been successfully reset.");
            log.debug("Password reset confirmation email sent to user: {}", userEmail);
        } catch (MessagingException e) {
            throw e;
        }

        log.info("Password reset completed successfully for user: {}", userEmail);
        return messageSource.getMessage("password.reset.success", null, getCurrentLocale());
    }
}