package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.Exception.InvalidTokenException;
import com.shopsavvy.shopshavvy.Exception.PasswordMismatchException;
import com.shopsavvy.shopshavvy.Exception.TokenNotFoundException;
import com.shopsavvy.shopshavvy.Exception.UserNotFoundException;
import com.shopsavvy.shopshavvy.dto.LoginResponseDTO;
import com.shopsavvy.shopshavvy.dto.ResetPasswordResponseDTO;
import com.shopsavvy.shopshavvy.dto.UserLoginDTO;
import com.shopsavvy.shopshavvy.model.token.AuthToken;
import com.shopsavvy.shopshavvy.model.token.TokenType;
import com.shopsavvy.shopshavvy.model.users.User;
import com.shopsavvy.shopshavvy.repository.AuthTokenRepository;
import com.shopsavvy.shopshavvy.repository.UserRepository;
import com.shopsavvy.shopshavvy.securityConfigurations.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Autowired
    public AuthenticationService(UserRepository userRepository,
                                 JwtService jwtService, AuthTokenRepository authTokenRepository,
                                 PasswordEncoder passwordEncoder,
                                 EmailService emailService){
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authTokenRepository = authTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public LoginResponseDTO authenticate(UserLoginDTO userLoginDTO) {
        if (!userRepository.existsByEmail(userLoginDTO.getEmail())) {
            throw new UserNotFoundException("User not found");
        }

        User user = userRepository.findByEmail(userLoginDTO.getEmail());
        if (!user.getIsActive()) {
            throw new RuntimeException("Account not verified. Please verify your account.");
        }

        if (user.isLocked()) {
            throw new RuntimeException("Account is locked. Please contact support.");
        }

        if(isPasswordCredentialExpired(userLoginDTO.getEmail())){
            throw new RuntimeException("Your password has been expired.");
        }


        if (!passwordEncoder.matches(userLoginDTO.getPassword(), user.getPassword())) {
            user.setInvalidAttemptCount(user.getInvalidAttemptCount() + 1);
            if (user.getInvalidAttemptCount() >= 3) {
                user.setLocked(true);
                try {
                    emailService.sendVerificationEmail(userLoginDTO.getEmail(), " Your ShopShavvy Account has been locked", "Your account has been locked. Please contact support.");
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            }
            userRepository.save(user);
            throw new BadCredentialsException("Invalid credentials. Please try again.");
        }

        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        String accessToken = jwtService.generateToken(userDetails, "access");
        String refreshToken = jwtService.generateToken(userDetails,"refresh");

        Claims claimsForAccessToken = jwtService.extractAllClaims(accessToken);
        Claims claimsForRefreshToken = jwtService.extractAllClaims(refreshToken);

        AuthToken authTokenForAccess = new AuthToken();
        authTokenForAccess.setUserEmail(userLoginDTO.getEmail());
        authTokenForAccess.setToken(accessToken);
        authTokenForAccess.setTokenType(TokenType.ACCESS);
        authTokenForAccess.setExpirationTime(claimsForAccessToken.getExpiration());
        authTokenRepository.save(authTokenForAccess);

        AuthToken refreshAuthToken = new AuthToken();
        refreshAuthToken.setUserEmail(userLoginDTO.getEmail());
        refreshAuthToken.setToken(refreshToken);
        refreshAuthToken.setTokenType(TokenType.REFRESH);
        refreshAuthToken.setExpirationTime(claimsForRefreshToken.getExpiration());
        authTokenRepository.save(refreshAuthToken);

        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getAuthority())
                .collect(Collectors.toSet());

        return new LoginResponseDTO(accessToken, refreshToken, userLoginDTO.getEmail(), roles);
    }


    public boolean isPasswordCredentialExpired(String email){
        LocalDateTime passwordLastUpdateDate = userRepository.findPasswordUpdateDateByEmail(email);
        User user = userRepository.findByEmail(email);
        if (passwordLastUpdateDate.isBefore(LocalDateTime.now().minus(3, ChronoUnit.MONTHS))) {
            user.setExpired(true);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public ResponseEntity<String> userLogout(String accessToken){
        AuthToken authToken = authTokenRepository.findByToken(accessToken)
                .orElseThrow(() -> new TokenNotFoundException("Access token not found"));

        if (authToken.getTokenType() != TokenType.ACCESS) {
            throw new InvalidTokenException("Invalid token.");
        }

        if (jwtService.isTokenExpired(accessToken)) {
            throw new InvalidTokenException("Access token has expired");
        }

        String email = jwtService.extractUsername(accessToken);
        authTokenRepository.deleteAccessTokenByEmail(email);

        return ResponseEntity.ok("You are logged out.");
    }

    public ResponseEntity<String> forgotPassword(String email) throws MessagingException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("Email is invalid.");
        }

        if(!user.getIsActive()){
            throw new RuntimeException("Account is not activated.");
        }

        authTokenRepository.deleteResetPasswordTokenByEmail(email);


        UserDetailsImpl userDetailsImpl = new UserDetailsImpl(userRepository.findByEmail(email));

        String resetPasswordToken = jwtService.generateToken(userDetailsImpl, "forgotPassword");

        emailService.sendVerificationEmail(email,"Password Reset Request", "To reset your password, click the link below:\n" + "http://localhost:8080/reset-password?token=" + resetPasswordToken);

        return ResponseEntity.ok("Password reset link has been sent to your email.");
    }


    public ResponseEntity<ResetPasswordResponseDTO> resetPassword(String token, String password, String confirmPassword) throws MessagingException {
        AuthToken authToken = authTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenNotFoundException("Token not found"));

        if(authToken.getTokenType() != TokenType.FORGOT_PASSWORD){
            throw new InvalidTokenException("Invalid token.");
        }

        if (jwtService.isTokenExpired(token)) {
            authTokenRepository.delete(authToken);
            throw new InvalidTokenException("Token is expired. Password is not updated.");
        }

        if (!password.equals(confirmPassword)) {
            throw new PasswordMismatchException("Confirm Passowrd is not same as Password.");
        }

        User user = userRepository.findByEmail(authToken.getUserEmail());
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        authTokenRepository.delete(authToken);

        emailService.sendVerificationEmail(user.getEmail(), "Password Reset Successful", "Your password has been successfully reset.");

        return ResponseEntity.ok(new ResetPasswordResponseDTO(token, password, confirmPassword));

    }

}
