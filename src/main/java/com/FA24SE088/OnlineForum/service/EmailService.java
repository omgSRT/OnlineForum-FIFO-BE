package com.FA24SE088.OnlineForum.service;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;
    private final String host = "smtp.gmail.com";
    private final int port = 587;
    private final String username = "your-email@gmail.com"; // Thay thế bằng email của bạn
    private final String password = "your-app-password"; // Thay thế bằng mật khẩu ứng dụng của bạn

    //    public void sendOtpEmail(String to, String subject, String text) {
    //        SimpleMailMessage message = new SimpleMailMessage();
    //        message.setTo(to);
    //        message.setSubject(subject);
    //        message.setText(text);
    //        mailSender.send(message);
    //    }
    public void sendEmail(String to, String subject, String message) {
        Email email = new HtmlEmail();
        email.setHostName(host);
        email.setSmtpPort(port);
        email.setAuthenticator(new DefaultAuthenticator(username, password));
        email.setStartTLSEnabled(true);
        try {
            email.setFrom(username);
            email.setSubject(subject);
            email.setMsg(message);
            email.addTo(to);
            email.send();
            System.out.println("Email sent successfully to " + to);
        } catch (EmailException e) {
            e.printStackTrace();
        }
    }
}