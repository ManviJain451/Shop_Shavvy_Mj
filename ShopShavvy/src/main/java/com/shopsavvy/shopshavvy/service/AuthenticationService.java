package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.Exception.*;
import com.shopsavvy.shopshavvy.dto.LoginResponseDTO;
import com.shopsavvy.shopshavvy.dto.ResetPasswordResponseDTO;
import com.shopsavvy.shopshavvy.dto.UserLoginDTO;
import com.shopsavvy.shopshavvy.model.token.AccessRefreshToken;
import com.shopsavvy.shopshavvy.model.token.AuthToken;
import com.shopsavvy.shopshavvy.model.users.Role;
import com.shopsavvy.shopshavvy.model.users.User;
import com.shopsavvy.shopshavvy.repository.AccessRefreshTokenRepository;
import com.shopsavvy.shopshavvy.repository.AuthTokenRepository;
import com.shopsavvy.shopshavvy.repository.RoleRepository;
import com.shopsavvy.shopshavvy.repository.UserRepository;
import com.shopsavvy.shopshavvy.securityConfigurations.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final RoleRepository roleRepository;
    private final AccessRefreshTokenRepository accessRefreshTokenRepository;

    @Autowired
    public AuthenticationService(UserRepository userRepository,
                                 JwtService jwtService, AuthTokenRepository authTokenRepository,
                                 PasswordEncoder passwordEncoder,
                                 EmailService emailService,
                                 RoleRepository roleRepository,
                                 AccessRefreshTokenRepository accessRefreshTokenRepository){
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authTokenRepository = authTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.roleRepository = roleRepository;
        this.accessRefreshTokenRepository = accessRefreshTokenRepository;
    }

    public String registerAdmin(User user) throws MessagingException {


        User use = new User();
        user.setEmail(user.getEmail());
        user.setFirstName(user.getFirstName());
        user.setLastName(user.getLastName());
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getMiddleName() != null && !user.getMiddleName().isBlank()) {
            user.setMiddleName(user.getMiddleName());
        }


        Role role = roleRepository.findByAuthority("ROLE_ADMIN");
        user.addRole(role);
        user.setIsActive(true);
        userRepository.save(user);

        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        String token = jwtService.generateToken(userDetails, "activation");
        Claims claims = jwtService.extractAllClaims(token);
        AuthToken authToken = new AuthToken();
        authToken.setUserEmail(user.getEmail());
        authToken.setToken(token);
        authToken.setTokenType("activation");
        authToken.setExpirationTime(claims.getExpiration());
        authTokenRepository.save(authToken);



        emailService.sendVerificationEmail(user.getEmail(), "Admin Account Created", "Admin account has been created. Admin account has been activated.");


        return "Admin has been registered";
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
            throw new LockedException("Account is locked. Please contact support.");
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

        AccessRefreshToken accessRefreshToken = new AccessRefreshToken();
        accessRefreshToken.setUserEmail(userLoginDTO.getEmail());
        accessRefreshToken.setAccessToken(accessToken);
        accessRefreshToken.setRefreshToken(refreshToken);
        accessRefreshTokenRepository.save(accessRefreshToken);

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

    @Transactional
    public ResponseEntity<String> userLogout(String accessToken){
        AccessRefreshToken accessRefreshToken = accessRefreshTokenRepository.findByAccessToken(accessToken)
                .orElseThrow(() -> new TokenNotFoundException("Access token not found"));

        Claims claims = jwtService.extractAllClaims(accessToken);

        if (!claims.get("type").equals("access")) {
            throw new InvalidTokenException("Invalid token.");
        }

        if (jwtService.isTokenExpired(accessToken)) {
            throw new InvalidTokenException("Access token has expired");
        }

        String email = jwtService.extractUsername(accessToken);
        accessRefreshTokenRepository.deleteByUserEmail(email);

        return ResponseEntity.ok("You are logged out.");
    }

    @Transactional
    public ResponseEntity<String> forgotPassword(String email) throws MessagingException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new InvalidEmailException("Email is invalid.");
        }

        if(!user.getIsActive()){
            throw new DeactivatedAccountException("Account is not activated.");
        }

        if(authTokenRepository.existsResetPasswordTokenByEmail(email)){
            authTokenRepository.deleteResetPasswordTokenByEmail(email);
        }

        UserDetailsImpl userDetailsImpl = new UserDetailsImpl(userRepository.findByEmail(email));

        String resetPasswordToken = jwtService.generateToken(userDetailsImpl, "reset_password");

        Claims claimsForResetPasswordToken = jwtService.extractAllClaims(resetPasswordToken);

        AuthToken resetPasswordAuthToken = new AuthToken();
        resetPasswordAuthToken.setUserEmail(email);
        resetPasswordAuthToken.setToken(resetPasswordToken);
        resetPasswordAuthToken.setTokenType("reset_password");
        resetPasswordAuthToken.setExpirationTime(claimsForResetPasswordToken.getExpiration());
        authTokenRepository.save(resetPasswordAuthToken);

        emailService.sendVerificationEmail(email,"Password Reset Request", "To reset your password, click the link below:\n" + "http://localhost:8080/shop-shavvy/auth/reset-password?token=" + resetPasswordToken);

        return ResponseEntity.ok("Password reset link has been sent to your email.");
    }

    @Transactional
    public ResponseEntity<ResetPasswordResponseDTO> resetPassword(String resetPasswordtoken, String password, String confirmPassword) throws MessagingException {
        if(!authTokenRepository.existsByToken(resetPasswordtoken)){
            throw new InvalidTokenException("Token is not found");
        }
        Claims claims = jwtService.extractAllClaims(resetPasswordtoken);

        if (!claims.get("type").equals("reset_password")) {
            throw new InvalidTokenException("Invalid resetPassword token.");
        }

        if (jwtService.isTokenExpired(resetPasswordtoken)) {
            throw new InvalidTokenException("Token is expired. Password is not updated.");
        }

        if (!password.equals(confirmPassword)) {
            throw new PasswordMismatchException("Confirm Password is not same as Password.");
        }

        User user = userRepository.findByEmail(jwtService.extractUsername(resetPasswordtoken));
        if (user == null) {
            throw new UserNotFoundException("User not found.");
        }

        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);

        userRepository.save(user);
        authTokenRepository.deleteByToken(resetPasswordtoken);

        emailService.sendVerificationEmail(user.getEmail(), "Password Reset Successful", "Your password has been successfully reset.");

        return ResponseEntity.ok(new ResetPasswordResponseDTO(resetPasswordtoken, password, confirmPassword));
    }

}
