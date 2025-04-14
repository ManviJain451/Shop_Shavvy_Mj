package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.model.token.BlackListedToken;
import com.shopsavvy.shopshavvy.repository.BlackListedTokenRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class BlackListedTokenService {
    private final BlackListedTokenRepository blackListedTokenRepository;

    public void blacklistRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                // Check for the refresh token cookie
                if ("refreshToken".equals(cookie.getName())) {
                    BlackListedToken blackListedToken = BlackListedToken.builder()
                            .token(cookie.getValue())
                            .type("refresh").build();
                    blackListedTokenRepository.save(blackListedToken);
                    break;
                }
            }
        }
    }

    public void blacklistAccessToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                // Check for the refresh token cookie
                if ("accessToken".equals(cookie.getName())) {
                    BlackListedToken blackListedToken = BlackListedToken.builder()
                            .token(cookie.getValue())
                            .type("accessToken").build();
                    blackListedTokenRepository.save(blackListedToken);
                    break;
                }
            }
        }
    }
}
