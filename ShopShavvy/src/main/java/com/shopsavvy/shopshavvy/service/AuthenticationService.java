package com.shopsavvy.shopshavvy.service;

import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    @Autowired
    private EmailService emailService;

    public ResponseEntity<Void> verifyUser(String email) throws Exception{
        try {
            emailService.sendVerificationEmail(email, "Account Activated", "Your account has been successfully activated.");
        } catch (Exception e) {
            throw new Exception("Verification Email is not send");
        }
         return ResponseEntity.ok().build();
    }
}
