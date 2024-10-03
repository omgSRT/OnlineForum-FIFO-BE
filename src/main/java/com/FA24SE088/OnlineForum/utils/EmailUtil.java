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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@Component
public class EmailUtil {
    final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String username;

    //chưa làm async được vì sẽ bị chặn file gửi cùng mail
    public void sendEmail(List<String> toEmails,
                          String body,
                          String subject,
                          List<MultipartFile> attachments) {
        try {
            boolean hasSubject = subject != null && !subject.trim().isEmpty();
            boolean hasBody = body != null && !body.trim().isEmpty();
            boolean hasAttachments = attachments != null && !attachments.isEmpty();

            if (!hasSubject && !hasBody && !hasAttachments) {
                throw new AppException(ErrorCode.EMAIL_CONTENT_BLANK);
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(username);
            helper.setText(body == null ? "" : body);
            helper.setSubject(subject == null ? "" : subject);

            if (attachments != null) {
                for (MultipartFile attachment : attachments) {
                    if (attachment.isEmpty()) {
                        System.out.println("Empty attachment: " + attachment.getOriginalFilename());
                        continue;
                    }
                    String attachmentName = attachment.getOriginalFilename();
                    if (attachmentName == null || attachmentName.isBlank()) {
                        attachmentName = attachmentName + "_" + System.currentTimeMillis();
                    }
                    helper.addAttachment(attachmentName, attachment);
                }
            }

            if(toEmails.isEmpty()){
                throw new AppException(ErrorCode.TO_EMAIL_EMPTY);
            }
            for (String toEmail : toEmails) {
                helper.setTo(toEmail);
                mailSender.send(message);
            }

            System.out.println("All Mails Sent Successfully");
        } catch (MailException | MessagingException e) {
            throw new AppException(ErrorCode.SEND_MAIL_FAILED);
        }
    }
}
