package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.repository.AuthTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
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

    @Autowired
    private AuthTokenRepository authTokenRepository;

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

    public boolean isTokenValid(String token, UserDetails userDetails, String tokenType) {
        final String username = extractUsername(token);
        Claims claims = extractAllClaims(token);

//        System.out.println(">>>>>>token is validating here");
//        System.out.println(username.equals(userDetails.getUsername()) && !isTokenExpired(token) && tokenType.equals(claims.get(tokenType)));
//        System.out.println(claims.get(tokenType));
//        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token) && tokenType.equals(claims.get("type"));

        boolean isTokenInRepository = authTokenRepository.existsByToken(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token) && tokenType.equals(claims.get("type")) && isTokenInRepository;


    }

//    private boolean isTokenExpired(String token) {
//        return extractExpiration(token).before(new Date());
//    }

    public boolean isTokenExpired(String token) {
        Instant expirationInstant = extractExpiration(token).toInstant();
        Instant currentInstant = Instant.now();

        System.out.println("Token Expiry (UTC): " + expirationInstant);
        System.out.println("Current Time (UTC): " + currentInstant);

        return expirationInstant.isBefore(currentInstant);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
