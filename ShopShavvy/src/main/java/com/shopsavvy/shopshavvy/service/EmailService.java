package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.dto.UserRegistrationDTO;
import com.shopsavvy.shopshavvy.model.users.User;
import com.shopsavvy.shopshavvy.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailService {


    private final JavaMailSender emailSender;
    private final UserRepository userRepository;

    @Async
    public void sendVerificationEmail(String to, String subject, String text) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true);

        emailSender.send(message);
    }

    @Async
    public void  sendActivationLink(String email, String jwtToken) throws MessagingException {
        String activationLink = UriComponentsBuilder.fromHttpUrl("http://localhost:8080/shop-shavvy/activate")
                .queryParam("token", jwtToken)
                .toUriString();

        String subject = "Activate your account";
        String text = "Please activate your account by clicking the link: " + activationLink;

        sendVerificationEmail(email, subject, text);
    }

    public void sendPasswordChangeNotification(String email) throws MessagingException {
        try {
            var message = emailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            helper.setSubject("Password Changed Successfully");
            helper.setText("Your password has been changed successfully. If this was not you, please contact support immediately.");
            emailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email notification.", e);
        }
    }

}
