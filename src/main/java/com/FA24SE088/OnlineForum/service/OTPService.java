package com.FA24SE088.OnlineForum.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OTPService {

    private Map<String, String> otpData = new HashMap<>();

    public String generateOTP(String email) {
        Random random = new Random();
        String otp = String.format("%06d", random.nextInt(1000000));
        otpData.put(email, otp);
        return otp;
    }

    public boolean validateOTP(String email, String otp) {
        return otp.equals(otpData.get(email));
    }

    public void clearOTP(String email) {
        otpData.remove(email);
    }
}
