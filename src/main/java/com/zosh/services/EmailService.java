package com.zosh.services;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender javaMailSender;

    public void senderVerficationOtpEmail(String email, String otp) {
        if (javaMailSender != null) {
            try {
                MimeMessage mimeMessage = javaMailSender.createMimeMessage();
                MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");
                String subject = "Verify OTP - Apex Trading";
                String text = "Your Verification code is: " + otp;

                mimeMessageHelper.setSubject(subject);
                mimeMessageHelper.setText(text);
                mimeMessageHelper.setTo(email);

                javaMailSender.send(mimeMessage);
                System.out.println("Verification email sent successfully to: " + email);
                return;
            } catch (Exception e) {
                System.err.println("Email send failed: " + e.getMessage());
            }
        }

        // Fallback log for development / when SMTP credentials are not yet configured
        System.out.println("==================================================");
        System.out.println("OTP CODE FOR " + email + ": " + otp);
        System.out.println("==================================================");
    }
}
