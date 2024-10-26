package com.FA24SE088.OnlineForum.utils;

import com.FA24SE088.OnlineForum.dto.response.OtpResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Otp;
import com.FA24SE088.OnlineForum.enums.AccountStatus;
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
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@Component
public class OtpUtil {
    final UnitOfWork unitOfWork;

    public Otp generateOtp(String email){
        Random random = new Random();
        int randomNumber = random.nextInt(9999);
        String otp = String.format("%04d", randomNumber);

        Otp otp1 = Otp.builder()
                .email(email)
                .otpEmail(otp)
                .createDate(new Date())
                .build();
        unitOfWork.getOtpRepository().save(otp1);

//        OtpResponse otpResponse = new OtpResponse();
//        otpResponse.setOtp(otp);
//        otpResponse.setCreatedDate(LocalDateTime.now());

        return otp1;
    }

    //email để tìm tài khoản tồn tại hay chưa
    //otp chứa thông tin người dùng nhập
    //otpResponse để so sánh otp người dùng nhập với thời gian có quá hạn hay chưa
//    public boolean verifyOTP(String email, String otp, OtpResponse otpResponse){
//        Account account = unitOfWork.getAccountRepository().findByEmail(email);
//        if(account == null){
//            throw new AppException(ErrorCode.ACCOUNT_NOT_FOUND);
//        }
//        boolean isVerified = otpResponse.getOtp().equals(otp)
//                && Duration.between(otpResponse.getCreatedDate(), LocalDateTime.now()).getSeconds() < (15*60);
//
//        if(!isVerified){
//            throw new AppException(ErrorCode.WRONG_OTP);
//        }
//
//        return true;
//    }

    public boolean verifyOTP(String email, String otp) {
        Account account = unitOfWork.getAccountRepository().findByEmail(email);
        if (account == null) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_FOUND);
        }

        if (account.getStatus().equals(AccountStatus.ACTIVE.name())){
            throw new AppException(ErrorCode.ACCOUNT_WAS_ACTIVE);
        }

        List<Otp> otpEntities = unitOfWork.getOtpRepository().findByEmail(email);
        if (otpEntities.isEmpty()) {
            throw new AppException(ErrorCode.OTP_NOT_FOUND);
        }

        Otp otpEntity = otpEntities.get(0);

        // Kiểm tra thời gian hết hạn (5 phút)
        LocalDateTime otpCreatedTime = otpEntity.getCreateDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        if (Duration.between(otpCreatedTime, LocalDateTime.now()).getSeconds() > (5 * 60)) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        // Kiểm tra mã OTP có khớp hay không
        boolean isVerified = otpEntity.getOtpEmail().equals(otp);
        if (!isVerified) {
            throw new AppException(ErrorCode.WRONG_OTP);
        }

        // Xóa OTP sau khi xác thực thành công
        unitOfWork.getOtpRepository().delete(otpEntity);
        return true;
    }

    public Otp resendOtp(String email) {
        Account account = unitOfWork.getAccountRepository().findByEmail(email);
        if (account == null) {
            throw new AppException(ErrorCode.EMAIL_NOT_FOUND);
        }
        if (account.getStatus().equals(AccountStatus.ACTIVE.name())){
            throw new AppException(ErrorCode.ACCOUNT_WAS_ACTIVE);
        }
        List<Otp> otpEntities = unitOfWork.getOtpRepository().findByEmail(email);
        if (otpEntities.isEmpty()) {
            return generateOtp(email);
        }

        // Lấy OTP mới nhất
        Otp otpEntity = otpEntities.get(0);
        LocalDateTime otpCreatedTime = otpEntity.getCreateDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        if (Duration.between(otpCreatedTime, LocalDateTime.now()).getSeconds() <= (5 * 60)) {
            throw new AppException(ErrorCode.OTP_STILL_VALID);
        }

        return generateOtp(email);
    }
}
