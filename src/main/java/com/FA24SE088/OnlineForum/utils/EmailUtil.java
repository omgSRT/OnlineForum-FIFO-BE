package com.FA24SE088.OnlineForum.utils;

import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@Component
public class EmailUtil {
    final JavaMailSender mailSender;

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    @Value("${spring.mail.username}")
    private String username;

    //chưa làm async được vì sẽ bị chặn file gửi cùng mail
    public void sendEmail(
            List<String> toEmails,
            String body,
            String subject,
            List<MultipartFile> attachments) {
        if (toEmails == null || toEmails.isEmpty()) {
            throw new AppException(ErrorCode.TO_EMAIL_EMPTY);
        }

        if ((subject == null || subject.trim().isEmpty()) &&
                (body == null || body.trim().isEmpty()) &&
                (attachments == null || attachments.isEmpty())) {
            throw new AppException(ErrorCode.EMAIL_CONTENT_BLANK);
        }

        for (String toEmail : toEmails) {
            if (!isValidEmail(toEmail)) {
                throw new AppException(ErrorCode.INVALID_EMAIL);
            }

            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);

                helper.setFrom(username);
                helper.setTo(toEmail);
                helper.setSubject(subject == null ? "" : subject);
                helper.setText(body == null ? "" : body);

                if (attachments != null) {
                    for (MultipartFile attachment : attachments) {
                        if (!attachment.isEmpty()) {
                            String attachmentName = attachment.getOriginalFilename();
                            if (attachmentName == null || attachmentName.isBlank()) {
                                attachmentName = "attachment_" + System.currentTimeMillis();
                            }
                            helper.addAttachment(attachmentName, attachment);
                        }
                    }
                }

                mailSender.send(message);
                System.out.println("Mail Sent Successfully to " + toEmail);

            } catch (MailException | MessagingException e) {
                log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
                throw new AppException(ErrorCode.SEND_MAIL_FAILED);
            }
        }
    }

    public void sendToAnEmail(
            String toEmail,
            String body,
            String subject,
            List<MultipartFile> attachments) {

        if ((subject == null || subject.trim().isEmpty()) &&
                (body == null || body.trim().isEmpty()) &&
                (attachments == null || attachments.isEmpty())) {
            throw new AppException(ErrorCode.EMAIL_CONTENT_BLANK);
        }

        if (!isValidEmail(toEmail)) {
            throw new AppException(ErrorCode.INVALID_EMAIL);
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(username);
            helper.setTo(toEmail);
            helper.setSubject(subject == null ? "" : subject);
            helper.setText(body == null ? "" : body);

            if (attachments != null) {
                for (MultipartFile attachment : attachments) {
                    if (!attachment.isEmpty()) {
                        String attachmentName = attachment.getOriginalFilename();
                        if (attachmentName == null || attachmentName.isBlank()) {
                            attachmentName = "attachment_" + System.currentTimeMillis();
                        }
                        helper.addAttachment(attachmentName, attachment);
                    }
                }
            }

            mailSender.send(message);
            System.out.println("Mail Sent Successfully to " + toEmail);

        } catch (MailException | MessagingException e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            throw new AppException(ErrorCode.SEND_MAIL_FAILED);
        }
    }
    public void sendSimpleEmail(String toEmail, String body, String subject) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom("thangckdt@gmail.com");
        mailSender.send(message);
    }

    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
