package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.dto.userDto.UserRegistrationDTO;
import com.shopsavvy.shopshavvy.exception.*;
import com.shopsavvy.shopshavvy.dto.loginDto.LoginResponseDTO;
import com.shopsavvy.shopshavvy.dto.loginDto.LoginRequestDTO;
import com.shopsavvy.shopshavvy.model.token.AuthToken;
import com.shopsavvy.shopshavvy.model.users.Role;
import com.shopsavvy.shopshavvy.model.users.User;
import com.shopsavvy.shopshavvy.repository.BlackListedTokenRepository;
import com.shopsavvy.shopshavvy.repository.AuthTokenRepository;
import com.shopsavvy.shopshavvy.repository.RoleRepository;
import com.shopsavvy.shopshavvy.repository.UserRepository;
import com.shopsavvy.shopshavvy.security.configurations.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
public class AuthenticationService {

    private static int MAX_INVALID_ATTEMPTS = 3;

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

    public String registerAdmin(UserRegistrationDTO userRegistrationDTO) throws MessagingException {
        User adminUser = User.builder()
                .email(userRegistrationDTO.getEmail())
                .firstName(userRegistrationDTO.getFirstName())
                .lastName(userRegistrationDTO.getLastName())
                .middleName((userRegistrationDTO.getMiddleName() != null && !userRegistrationDTO.getMiddleName().isBlank()) ? userRegistrationDTO.getMiddleName() : null)
                .password(passwordEncoder.encode(userRegistrationDTO.getPassword()))
                .isActive(true)
                .build();

        Role role = roleRepository.findByAuthority("ROLE_ADMIN");
        adminUser.addRole(role);

        userRepository.save(adminUser);

        UserDetailsImpl userDetails = new UserDetailsImpl(adminUser);
        String token = jwtService.generateToken(userDetails, "activation");
        Claims claims = jwtService.extractAllClaims(token);

        AuthToken authToken = AuthToken.builder()
                .userEmail(adminUser.getEmail())
                .token(token)
                .tokenType("activation")
                .expirationTime(claims.getExpiration())
                .build();

        authTokenRepository.save(authToken);

        emailService.sendVerificationEmail(
                adminUser.getEmail(),
                "Admin Account Created",
                "Admin account has been created. Admin account has been activated."
        );

        return messageSource.getMessage("admin.register.success", null, getCurrentLocale());
    }

    public LoginResponseDTO authenticate(LoginRequestDTO loginRequestDTO, HttpServletResponse httpServletResponse) throws InvalidRoleException, MessagingException {
        User user = userRepository.findByEmail(loginRequestDTO.getEmail())
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("user.not.found", null, getCurrentLocale())));

        if (user.isLocked()) {
            throw new LockedException(messageSource.getMessage("account.locked", null, getCurrentLocale()));
        }

        if (!user.getIsActive()) {
            throw new AlreadyDeactivatedException(messageSource.getMessage("account.not.activated", null, getCurrentLocale()));
        }

        if (isPasswordCredentialExpired(loginRequestDTO.getEmail())) {
            throw new RuntimeException(messageSource.getMessage("password.expired", null, getCurrentLocale()));
        }

        if (!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
            user.setInvalidAttemptCount(user.getInvalidAttemptCount() + 1);
            if (user.getInvalidAttemptCount() >= MAX_INVALID_ATTEMPTS) {
                user.setLocked(true);
                userRepository.save(user);
                emailService.sendVerificationEmail(loginRequestDTO.getEmail(),
                        "Your ShopShavvy Account has been locked",
                        "Your account has been locked. Please contact support.");
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
                .map(role -> role.getAuthority())
                .collect(Collectors.toSet());

        return new LoginResponseDTO(accessToken, refreshToken, loginRequestDTO.getEmail(), roles);
    }

    public boolean isPasswordCredentialExpired(String email) {
        LocalDateTime passwordLastUpdateDate = userRepository.findPasswordUpdateDateByEmail(email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("user.not.found", null, getCurrentLocale())));
        if (passwordLastUpdateDate.isBefore(LocalDateTime.now().minus(3, ChronoUnit.MONTHS))) {
            user.setExpired(true);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public String userLogout(String accessToken, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws MessagingException {
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

        return messageSource.getMessage("user.logout.success", null, getCurrentLocale());
    }

    @Transactional
    public String refreshToken(String refreshToken, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws RuntimeException, MessagingException {
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

        return newAccessToken;
    }

    @Transactional
    public String forgotPassword(String email) throws MessagingException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("user.not.found", null, getCurrentLocale())));

        if (!user.getIsActive()) {
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

        emailService.sendVerificationEmail(email,
                "Password Reset Request",
                "To reset your password, click the link below:\n" + "http://localhost:8080/shop-shavvy/auth/reset-password?token=" + resetPasswordToken);

        return messageSource.getMessage("password.reset.link.sent", null, getCurrentLocale());
    }

    @Transactional
    public String resetPassword(String resetPasswordtoken, String password, String confirmPassword) throws MessagingException {
        String userEmail = jwtService.extractUsername(resetPasswordtoken);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("user.not.found", null, getCurrentLocale())));

        UserDetailsImpl userDetailsImpl = new UserDetailsImpl(user);
        jwtService.isTokenValid(resetPasswordtoken, userDetailsImpl, "reset_password");

        if (!password.equals(confirmPassword)) {
            throw new PasswordMismatchException(messageSource.getMessage("password.mismatch", null, getCurrentLocale()));
        }

        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);
        userRepository.save(user);

        authTokenRepository.deleteByToken(resetPasswordtoken);

        emailService.sendVerificationEmail(user.getEmail(), "Password Reset Successful", "Your password has been successfully reset.");

        return messageSource.getMessage("password.reset.success", null, getCurrentLocale());
    }

}
