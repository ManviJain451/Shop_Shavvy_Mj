package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.Exception.UserNotFoundException;
import com.shopsavvy.shopshavvy.dto.LoginResponseDTO;
import com.shopsavvy.shopshavvy.dto.UserLoginDTO;
import com.shopsavvy.shopshavvy.model.token.AuthToken;
import com.shopsavvy.shopshavvy.model.token.TokenType;
import com.shopsavvy.shopshavvy.model.users.User;
import com.shopsavvy.shopshavvy.repository.AuthTokenRepository;
import com.shopsavvy.shopshavvy.repository.UserRepository;
import com.shopsavvy.shopshavvy.securityConfigurations.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationService(UserRepository userRepository,
                                 JwtService jwtService, AuthTokenRepository authTokenRepository,
                                 PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authTokenRepository = authTokenRepository;
        this.passwordEncoder = passwordEncoder;
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

        if(user.isCredentialsExpired()){
            throw new RuntimeException("Your password has been expired.");
        }


        if (!passwordEncoder.matches(userLoginDTO.getPassword(), user.getPassword())) {
            user.setInvalidAttemptCount(user.getInvalidAttemptCount() + 1);
            if (user.getInvalidAttemptCount() >= 3) {
                user.setLocked(true);
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

}
