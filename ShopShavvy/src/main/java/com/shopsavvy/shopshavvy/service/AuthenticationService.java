package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.exception.*;
import com.shopsavvy.shopshavvy.dto.LoginResponseDTO;
import com.shopsavvy.shopshavvy.dto.ResetPasswordResponseDTO;
import com.shopsavvy.shopshavvy.dto.UserLoginDTO;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
@Transactional
public class AuthenticationService {

    @Value("${jwt.expiration-time.accessToken}")
    private long accessTokenExpirationTime;

    @Value("${jwt.expiration-time.refreshToken}")
    private long refreshTokenExpirationTime;

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RoleRepository roleRepository;
    private final BlackListedTokenRepository blackListedTokenRepository;
    private  final BlackListedTokenService blackListedTokenService;

    @Autowired
    public AuthenticationService(UserRepository userRepository,
                                 JwtService jwtService, AuthTokenRepository authTokenRepository,
                                 PasswordEncoder passwordEncoder,
                                 EmailService emailService,
                                 RoleRepository roleRepository,
                                 BlackListedTokenRepository blackListedTokenRepository,
                                 BlackListedTokenService blackListedTokenService){
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authTokenRepository = authTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.roleRepository = roleRepository;
        this.blackListedTokenRepository = blackListedTokenRepository;
        this.blackListedTokenService = blackListedTokenService;
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

    public LoginResponseDTO authenticate(UserLoginDTO userLoginDTO, HttpServletResponse httpServletResponse) throws InvalidRoleException, MessagingException {
        if (!userRepository.existsByEmail(userLoginDTO.getEmail())) {
            throw new UserNotFoundException("User not found");
        }

        User user = userRepository.findByEmail(userLoginDTO.getEmail());

        if (user.isLocked()) {
            throw new LockedException("Account is locked. Please contact support.");
        }

        if (!user.getIsActive()) {
            throw new DeactivatedAccountException("Account is not activated. Please activate your account.");
        }

        if(isPasswordCredentialExpired(userLoginDTO.getEmail())){
            throw new RuntimeException("Your password has been expired.");
        }

        if (!passwordEncoder.matches(userLoginDTO.getPassword(), user.getPassword())) {
            user.setInvalidAttemptCount(user.getInvalidAttemptCount() + 1);
            if (user.getInvalidAttemptCount() >= 3) {
                user.setLocked(true);
                userRepository.save(user);
                emailService.sendVerificationEmail(userLoginDTO.getEmail(), " Your ShopShavvy Account has been locked", "Your account has been locked. Please contact support.");
            } else {
                throw new BadCredentialsException("Invalid credentials");
            }
            userRepository.save(user);


        }

        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        String accessToken = jwtService.generateToken(userDetails, "access");
        String refreshToken = jwtService.generateToken(userDetails,"refresh");

        Claims claimsForAccessToken = jwtService.extractAllClaims(accessToken);
        Claims claimsForRefreshToken = jwtService.extractAllClaims(refreshToken);

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


    public ResponseEntity<String> userLogout(String accessToken, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws MessagingException {
        if(blackListedTokenRepository.existsByToken(accessToken)){
            throw new TokenNotFoundException("Access token is not found");
        }

        String userEmail = jwtService.extractUsername(accessToken);
        User user = userRepository.findByEmail(userEmail);
        UserDetailsImpl userDetailsImpl = new UserDetailsImpl(user);
        if (!jwtService.isTokenValid(accessToken, userDetailsImpl, "access")) {
            throw new InvalidTokenException("Access token has expired");
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


        return ResponseEntity.ok("You are logged out.");
    }

    @Transactional
    public ResponseEntity<String> forgotPassword(String email) throws MessagingException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UserNotFoundException("User not found.");
        }

        if(!user.getIsActive()){
            throw new DeactivatedAccountException("Account is not activated.");
        }

        authTokenRepository.deleteResetPasswordTokenByEmail(email);
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

        String userEmail = jwtService.extractUsername(resetPasswordtoken);
        User user = userRepository.findByEmail(userEmail);
        UserDetailsImpl userDetailsImpl = new UserDetailsImpl(user);
        jwtService.isTokenValid(resetPasswordtoken, userDetailsImpl, "reset_password");

        if (!password.equals(confirmPassword)) {
                throw new PasswordMismatchException("Confirm Password is not same as Password.");
        }
        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);
        userRepository.save(user);

        authTokenRepository.deleteByToken(resetPasswordtoken);

        emailService.sendVerificationEmail(user.getEmail(), "Password Reset Successful", "Your password has been successfully reset.");
        return ResponseEntity.ok(new ResetPasswordResponseDTO(resetPasswordtoken, password, confirmPassword));

    }

}