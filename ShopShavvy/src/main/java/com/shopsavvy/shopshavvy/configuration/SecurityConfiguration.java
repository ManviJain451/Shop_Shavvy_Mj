package com.shopsavvy.shopshavvy.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsavvy.shopshavvy.filter.JwtAuthenticationFilter;
import com.shopsavvy.shopshavvy.utilities.ErrorDetails;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/customers/**").hasRole("CUSTOMER")
                        .requestMatchers("/api/v1/sellers/**").hasRole("SELLER")
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/auth/register/admin").hasRole("ADMIN")
                        .requestMatchers("/api/v1/users/**").hasAnyRole("ADMIN", "CUSTOMER", "SELLER")
                        .anyRequest().permitAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");

            String message = messageSource.getMessage("error.unauthorized", null, LocaleContextHolder.getLocale());
            ErrorDetails errorDetails = new ErrorDetails(
                    LocalDateTime.now(),
                    message,
                    request.getRequestURI()
            );

            objectMapper.writeValue(response.getWriter(), errorDetails);
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");

            String message = messageSource.getMessage("error.forbidden", null, LocaleContextHolder.getLocale());
            ErrorDetails errorDetails = new ErrorDetails(
                    LocalDateTime.now(),
                    message,
                    request.getRequestURI()
            );

            objectMapper.writeValue(response.getWriter(), errorDetails);
        };
    }



}
