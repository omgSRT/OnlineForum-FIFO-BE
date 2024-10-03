package com.FA24SE088.OnlineForum.utils;

import com.FA24SE088.OnlineForum.dto.response.OtpResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@Component
public class OtpUtil {
    final UnitOfWork unitOfWork;

    public OtpResponse generateOtp(){
        Random random = new Random();
        int randomNumber = random.nextInt(9999);
        String otp = String.format("%04d", randomNumber);

        OtpResponse otpResponse = new OtpResponse();
        otpResponse.setOtp(otp);
        otpResponse.setCreatedDate(LocalDateTime.now());

        return otpResponse;
    }

    public boolean verifyOTP(String email, String otp, OtpResponse otpResponse){
        Account account = unitOfWork.getAccountRepository().findByEmail(email);
        if(account == null){
            throw new AppException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        boolean isVerified = otpResponse.getOtp().equals(otp)
                && Duration.between(otpResponse.getCreatedDate(), LocalDateTime.now()).getSeconds() < (15*60);

        if(!isVerified){
            throw new AppException(ErrorCode.WRONG_OTP);
        }

        return true;
    }
}
