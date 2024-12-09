package com.FA24SE088.OnlineForum.utils;

import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Otp;
import com.FA24SE088.OnlineForum.enums.AccountStatus;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.repository.AccountRepository;
import com.FA24SE088.OnlineForum.repository.OtpRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
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
    final OtpRepository otpRepository;
    final AccountRepository accountRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public Otp generateOtp(String email) {
        Random random = new Random();
        int randomNumber = random.nextInt(9999);
        String otp = String.format("%04d", randomNumber);

        Otp otp1 = Otp.builder()
                .email(email)
                .otpEmail(otp)
                .createDate(new Date())
                .build();
        otpRepository.save(otp1);
        return otp1;
    }

    public String generateOtpRedis(String email) {
        Random random = new Random();
        int randomNumber = random.nextInt(9999);
        String otp = String.format("%04d", randomNumber);

        redisTemplate.opsForValue().set(email, otp, Duration.ofMinutes(5));
        return otp;
    }

    public boolean verifyOTPRedis(String email, String otp) {
        String storedOtp = redisTemplate.opsForValue().get(email);
        Account account = accountRepository.findByEmail(email);
        if (account == null) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_FOUND);
        }

        if (account.getStatus().equals(AccountStatus.ACTIVE.name())) {
            throw new AppException(ErrorCode.ACCOUNT_WAS_ACTIVE);
        }

        if (storedOtp == null) {
            throw new AppException(ErrorCode.OTP_NOT_FOUND);
        }
        if (!storedOtp.equals(otp)) {
            throw new AppException(ErrorCode.WRONG_OTP);
        }
        redisTemplate.delete(email);
        return true;
    }


    //email để tìm tài khoản tồn tại hay chưa
    //otp chứa thông tin người dùng nhập
    //otpResponse để so sánh otp người dùng nhập với thời gian có quá hạn hay chưa
//    public boolean verifyOTP(String email, String otp, OtpResponse otpResponse){
//        Account account = accountRepository.findByEmail(email);
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
//        String storedOtp = redisTemplate.opsForValue().get(email);
//
//        if (storedOtp == null) {
//            throw new AppException(ErrorCode.OTP_NOT_FOUND);
//        }
//
//        // Kiểm tra mã OTP có khớp không
//        if (!storedOtp.equals(otp)) {
//            throw new AppException(ErrorCode.WRONG_OTP);
//        }
        //-------------------------------------------------------------------
        Account account = accountRepository.findByEmail(email);
        if (account == null) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_FOUND);
        }

        if (account.getStatus().equals(AccountStatus.ACTIVE.name())) {
            throw new AppException(ErrorCode.ACCOUNT_WAS_ACTIVE);
        }

        List<Otp> otpEntities = otpRepository.findByEmail(email);
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
        otpRepository.delete(otpEntity);
//        redisTemplate.delete(email);
        return true;
    }

    public boolean verifyOTPForForgetPassword(String email, String otp) {
        Account account = accountRepository.findByEmail(email);
        if (account == null) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_FOUND);
        }

        List<Otp> otpEntities = otpRepository.findByEmail(email);
        if (otpEntities.isEmpty()) {
            throw new AppException(ErrorCode.OTP_NOT_FOUND);
        }

        Otp otpEntity = otpEntities.get(0);

        LocalDateTime otpCreatedTime = otpEntity.getCreateDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        if (Duration.between(otpCreatedTime, LocalDateTime.now()).getSeconds() > (5 * 60)) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        boolean isVerified = otpEntity.getOtpEmail().equals(otp);
        if (!isVerified) {
            throw new AppException(ErrorCode.WRONG_OTP);
        }

        otpRepository.delete(otpEntity);
        return true;
    }

    public Otp resendOtp(String email) {
        Account account = accountRepository.findByEmail(email);
        if (account == null) {
            throw new AppException(ErrorCode.EMAIL_NOT_FOUND);
        }
        if (account.getStatus().equals(AccountStatus.ACTIVE.name())) {
            throw new AppException(ErrorCode.ACCOUNT_WAS_ACTIVE);
        }
        List<Otp> otpEntities = otpRepository.findByEmail(email);
        if (otpEntities.isEmpty()) {
            generateOtp(email);
        }

        // Lấy OTP mới nhất
        Otp otpEntity = otpEntities.get(0);
        LocalDateTime otpCreatedTime = otpEntity.getCreateDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        if (Duration.between(otpCreatedTime, LocalDateTime.now()).getSeconds() <= (5 * 60)) {
            throw new AppException(ErrorCode.OTP_STILL_VALID);
        }
        Otp otp = generateOtp(email);
        return otp;
    }
}
