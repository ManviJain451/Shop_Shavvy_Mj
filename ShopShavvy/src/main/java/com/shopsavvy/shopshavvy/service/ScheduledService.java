package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.model.user.User;
import com.shopsavvy.shopshavvy.repository.AuthTokenRepository;
import com.shopsavvy.shopshavvy.repository.BlackListedTokenRepository;
import com.shopsavvy.shopshavvy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduledService {

    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;
    private final BlackListedTokenRepository blackListedTokenRepository;

    @Scheduled(fixedRate = 60*60*1000)
    public void unlockLockedUsers() {
        List<User> lockedUsers = userRepository.findAllByIsLocked(true);
        for (User user : lockedUsers) {
            user.setLocked(false);
            user.setInvalidAttemptCount(0);
            userRepository.save(user);
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void deleteExpiredTokens() {
        Date now = new Date();
        authTokenRepository.deleteByExpirationTimeBefore(now);
    }
}
