package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.exception.InvalidTokenException;
import com.shopsavvy.shopshavvy.exception.TokenExpiredException;
import com.shopsavvy.shopshavvy.exception.TokenNotFoundException;
import com.shopsavvy.shopshavvy.exception.UserNotFoundException;
import com.shopsavvy.shopshavvy.model.users.User;
import com.shopsavvy.shopshavvy.repository.BlackListedTokenRepository;
import com.shopsavvy.shopshavvy.repository.AuthTokenRepository;
import com.shopsavvy.shopshavvy.repository.UserRepository;
import com.shopsavvy.shopshavvy.configuration.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtService {
    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.expiration-time.accessToken}")
    private long accessTokenExpirationTime;

    @Value("${jwt.expiration-time.refreshToken}")
    private long refreshTokenExpirationTime;

    @Value("${jwt.expiration-time.activationToken}")
    private long activateTokenExpirationTime;

    @Value("${jwt.expiration-time.resetPasswordToken}")
    private long resetPasswordTokenTime;

    private final AuthTokenRepository authTokenRepository;
    private final BlackListedTokenRepository blackListedTokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final Logger logger= LoggerFactory.getLogger(JwtService.class);
    private final MessageSource messageSource;

    private Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }


    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails, String tokenType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        return generateToken(claims, userDetails, tokenType);
    }


    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, String tokenType) {
        long expirationTime;
        switch (tokenType) {
            case "refresh":
                expirationTime = refreshTokenExpirationTime;
                break;
            case "activation":
                expirationTime = activateTokenExpirationTime;
                break;
            case "access":
                expirationTime = accessTokenExpirationTime;
                break;
            default:
                expirationTime = resetPasswordTokenTime;
                break;

        }
        return buildToken(extraClaims, userDetails, expirationTime, tokenType);
    }



    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration, String tokenType
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .claim("type", tokenType)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    public boolean isTokenValid(String token, UserDetails userDetails, String tokenType) throws MessagingException {
        final String username = extractUsername(token);
        final Claims claims = extractAllClaims(token);

        if (!username.equals(userDetails.getUsername())) {
            throw new InvalidTokenException(messageSource.getMessage("error.token.user.mismatch", null, getCurrentLocale()));
        }

        String typeFromToken = (String) claims.get("type");
        if (!tokenType.equals(typeFromToken)) {
            Object[] args = {tokenType, typeFromToken};
            throw new InvalidTokenException(messageSource.getMessage("error.token.type.mismatch", args, getCurrentLocale()));
        }

        if (isTokenExpired(token)) {
            if ("activation".equals(tokenType)) {
                User user = userRepository.findByEmail(username)
                        .orElseThrow(() -> new UserNotFoundException(
                                messageSource.getMessage("error.user.not.found", null, getCurrentLocale())));
                authTokenRepository.deleteByToken(token);
                sendActivationLinkIfTokenIsExpired(username);

                throw new TokenExpiredException(
                        messageSource.getMessage("error.token.activation.expired", null, getCurrentLocale()));
            }

            throw new TokenExpiredException(messageSource.getMessage("error.token.expired", null, getCurrentLocale()));
        }


        boolean tokenExists;
        switch (tokenType) {
            case "activation":
            case "reset_password":
                tokenExists = authTokenRepository.existsByToken(token);
                if (!tokenExists) {
                    throw new TokenNotFoundException(
                            messageSource.getMessage("error.token.not.found", null, getCurrentLocale()));
                }
                break;

            case "access":
            default:
                boolean isBlacklisted = blackListedTokenRepository.existsByToken(token);
                if (isBlacklisted) {
                    throw new TokenNotFoundException(
                            messageSource.getMessage("error.token.blacklisted", null, getCurrentLocale()));
                }
                break;
        }

        return true;
    }


    public boolean isTokenExpired(String token) {
        Instant expirationInstant = extractExpiration(token).toInstant();
        Instant currentInstant = Instant.now();
        return expirationInstant.isBefore(currentInstant);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Claims extractAllClaims(String token) {
        try{
            return Jwts
                    .parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }catch (JwtException e) {
            throw new InvalidTokenException(messageSource.getMessage("error.token.invalid", null, getCurrentLocale()));
        }
    }


    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public void sendActivationLinkIfTokenIsExpired(String email) throws MessagingException {
        UserDetailsImpl userDetailsImpl = userDetailsServiceImpl.loadUserByUsername(email);
        String newActivationToken = generateToken(userDetailsImpl, "activation");
        emailService.sendActivationLink(email, newActivationToken);

    }
}
