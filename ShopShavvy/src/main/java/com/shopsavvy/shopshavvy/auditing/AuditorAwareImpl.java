package com.shopsavvy.shopshavvy.auditing;

import com.shopsavvy.shopshavvy.exception.UserNotFoundException;
import com.shopsavvy.shopshavvy.model.users.User;
import com.shopsavvy.shopshavvy.repository.UserRepository;
import com.shopsavvy.shopshavvy.security.configurations.UserDetailsImpl;
import com.shopsavvy.shopshavvy.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken
        ) {
            return Optional.empty();
        }

        UserDetailsImpl userDetailsImpl = (UserDetailsImpl) authentication.getPrincipal();
        return Optional.ofNullable(userDetailsImpl.getId());
    }
}

