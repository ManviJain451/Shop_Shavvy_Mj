package com.shopsavvy.shopshavvy.service;

import jakarta.mail.MessagingException;
import jakarta.mail.SendFailedException;
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
    private final MessageSource messageSource;

    private Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    private String wrapWithEmailTemplate(String title, String bodyContent) {
        return """
            <html>
                <body style="font-family: Arial, sans-serif; background-color: #f8f9fa; padding: 20px;">
                    <div style="max-width: 600px; margin: auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                        <h1 style="color: #007bff;">Shop-Shavvy</h1>
                        <h2 style="color: #343a40;">%s</h2>
                        <div style="margin-top: 10px; color: #495057;">%s</div>
                        <hr style="margin: 30px 0;">
                        <p style="font-size: 0.9em; color: #6c757d;">This is an automated email from Shop-Shavvy. Please do not reply directly to this email.</p>
                    </div>
                </body>
            </html>
            """.formatted(title, bodyContent);
    }

    @Async
    public void sendVerificationEmail(String to, String subject, String text) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(wrapWithEmailTemplate(subject, text), true);

        emailSender.send(message);
    }

    @Async
    public void sendActivationLink(String email, String jwtToken) throws MessagingException {
        String activationLink = UriComponentsBuilder.fromHttpUrl("http://localhost:8080/api/v1/auth/activate/customer")
                .queryParam("token", jwtToken)
                .toUriString();

        String subject = "Activate your Shop-Shavvy Account";
        String body = """
            <p>Welcome to Shop-Shavvy! Please activate your account by clicking the button below:</p>
            <p><a href="%s" style="display: inline-block; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px;">Activate Account</a></p>
            """.formatted(activationLink);

        sendVerificationEmail(email, subject, body);
    }

    @Async
    public void sendPasswordChangeNotification(String email) throws MessagingException {
        try {
            String subject = "Password Changed Successfully";
            String body = """
                <p>Your password has been changed successfully.</p>
                <p>If this was not you, please contact our support team immediately.</p>
                """;

            sendVerificationEmail(email, subject, body);
        } catch (SendFailedException e) {
            throw new SendFailedException(
                    messageSource.getMessage("error.email.not.send", null, getCurrentLocale()), e);
        }
    }

    @Async
    public void sendProductStatusUpdateEmail(String email, String productName, boolean isActive,
                                             String brand, String description) throws SendFailedException {
        try {
            String subject = isActive ? "Product Activation Notification" : "Product Deactivation Notification";
            String contentTitle = isActive ? "Product Successfully Activated" : "Product Has Been Deactivated";
            String body = """
                <p>Product Details:</p>
                <ul>
                    <li><strong>Product Name:</strong> %s</li>
                    <li><strong>Brand:</strong> %s</li>
                    <li><strong>Description:</strong> %s</li>
                </ul>
                <p>%s</p>
                """.formatted(productName, brand, description,
                    isActive ? "Your product has been activated and is now visible to customers." :
                            "Your product has been deactivated and is no longer visible to customers.");

            sendVerificationEmail(email, subject, wrapWithEmailTemplate(contentTitle, body));

        } catch (MessagingException e) {
            throw new SendFailedException(
                    messageSource.getMessage("error.email.not.send", null, getCurrentLocale()), e);
        }
    }


    @Async
    public void sendAddProductEmail(String email, String productName, boolean isActive,
                                             String brand, String description) throws SendFailedException {
        try {
            String subject = "Product Added";
            String contentTitle = "Verify, and make it active.";
            String body = """
                <p>Product Details:</p>
                <ul>
                    <li><strong>Product Name:</strong> %s</li>
                    <li><strong>Brand:</strong> %s</li>
                    <li><strong>Description:</strong> %s</li>
                </ul>
                <p>%s</p>
                """.formatted(productName, brand, description, "Verify its details and activate the product.");

            sendVerificationEmail(email, subject, wrapWithEmailTemplate(contentTitle, body));

        } catch (MessagingException e) {
            throw new SendFailedException(
                    messageSource.getMessage("error.email.not.send", null, getCurrentLocale()), e);
        }
    }
}
