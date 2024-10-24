package com.FA24SE088.OnlineForum.controller;

import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.enums.SuccessReturnMessage;
import com.FA24SE088.OnlineForum.service.EmailSenderService;
import com.FA24SE088.OnlineForum.utils.EmailUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/email")
@Slf4j
public class EmailController {
    final EmailUtil emailUtil;
    @Autowired
    private EmailSenderService emailSenderService;
    @Operation(summary = "Send An Email to multiple participants")
    @PostMapping(value = "/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> sendMail(

            @RequestParam(required = false) String emails,
            @RequestParam(required = false) String body,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) List<MultipartFile> attachments) {
//        emailUtil.sendEmail(emails, body, subject, attachments);
        emailSenderService.sendSimpleEmail(emails,body,subject);
        return ApiResponse.<Void>builder()
                .message(SuccessReturnMessage.SEND_SUCCESS.getMessage())
                .build();
    }
}
