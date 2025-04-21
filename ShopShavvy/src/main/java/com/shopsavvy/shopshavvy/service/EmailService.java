package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmailService {


    private final JavaMailSender emailSender;
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    private Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }


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
            throw new RuntimeException( messageSource.getMessage("error.email.not.send", null, getCurrentLocale()), e);
        }
    }

    @Async
    public void sendProductStatusUpdateEmail(String email, String productName, boolean isActive,
                                             String brand, String description) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(email);
            helper.setSubject(isActive ? "Product Activation Notification" : "Product Deactivation Notification");

            String emailText = buildProductStatusEmail(productName, isActive, brand, description);
            helper.setText(emailText, true);

            emailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String buildProductStatusEmail(String productName, boolean isActive, String brand, String description) {
        StringBuilder builder = new StringBuilder();
        builder.append("<html><body>");
        builder.append("<h2>").append(isActive ? "Product Successfully Activated" : "Product Has Been Deactivated").append("</h2>");
        builder.append("<p>Product Details:</p>");
        builder.append("<ul>");
        builder.append("<li>Product Name: ").append(productName).append("</li>");
        builder.append("<li>Brand: ").append(brand).append("</li>");
        builder.append("<li>Description: ").append(description).append("</li>");
        builder.append("</ul>");
        builder.append("<p>").append(isActive ?
                "Your product has been activated and is now visible to customers." :
                "Your product has been deactivated and is no longer visible to customers.").append("</p>");
        builder.append("</body></html>");

        return builder.toString();
    }

}
