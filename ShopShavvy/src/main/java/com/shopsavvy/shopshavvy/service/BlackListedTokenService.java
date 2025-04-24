package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.model.token.BlackListedToken;
import com.shopsavvy.shopshavvy.repository.BlackListedTokenRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class BlackListedTokenService {
    private final BlackListedTokenRepository blackListedTokenRepository;

    public void blacklistRefreshToken(HttpServletRequest request) {
        log.info("Attempting to blacklist refresh token");
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    BlackListedToken blackListedToken = BlackListedToken.builder()
                            .token(cookie.getValue())
                            .type("refresh").build();
                    blackListedTokenRepository.save(blackListedToken);
                    log.info("Successfully blacklisted refresh token");
                    break;
                }
            }
        } else {
            log.warn("No cookies found in request for blacklisting refresh token");
        }
    }

    public void blacklistAccessToken(HttpServletRequest request) {
        log.info("Attempting to blacklist access token");
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    BlackListedToken blackListedToken = BlackListedToken.builder()
                            .token(cookie.getValue())
                            .type("accessToken").build();
                    blackListedTokenRepository.save(blackListedToken);
                    log.info("Successfully blacklisted access token");
                    break;
                }
            }
        } else {
            log.warn("No cookies found in request for blacklisting access token");
        }
    }
}