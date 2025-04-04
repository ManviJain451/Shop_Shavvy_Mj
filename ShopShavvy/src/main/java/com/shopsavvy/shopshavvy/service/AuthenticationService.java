package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.Exception.UserNotFoundException;
import com.shopsavvy.shopshavvy.dto.UserLoginDTO;
import com.shopsavvy.shopshavvy.model.token.AuthToken;
import com.shopsavvy.shopshavvy.model.token.TokenType;
import com.shopsavvy.shopshavvy.model.users.User;
import com.shopsavvy.shopshavvy.repository.AuthTokenRepository;
import com.shopsavvy.shopshavvy.repository.UserRepository;
import com.shopsavvy.shopshavvy.securityConfigurations.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthTokenRepository authTokenRepository;

    @Autowired
    public AuthenticationService(UserRepository userRepository,
                                 JwtService jwtService, AuthTokenRepository authTokenRepository){
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authTokenRepository = authTokenRepository;
    }

    public String authenticate(UserLoginDTO userLoginDTO) {
        if (!userRepository.existsByEmail(userLoginDTO.getEmail())) {
            throw new UserNotFoundException("User not found");
        }

        User user = userRepository.findByEmail(userLoginDTO.getEmail());
        if (!user.getIsActive()) {
            throw new RuntimeException("Account not verified. Please verify your account.");
        }


        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        String token = jwtService.generateToken(userDetails, "access");

        Claims claims = jwtService.extractAllClaims(token);

        AuthToken authToken = new AuthToken();
        authToken.setUserEmail(userLoginDTO.getEmail());
        authToken.setToken(token);
        authToken.setTokenType(TokenType.ACCESS);
        authToken.setExpirationTime(claims.getExpiration());
        authTokenRepository.save(authToken);

        return token;
    }

}
