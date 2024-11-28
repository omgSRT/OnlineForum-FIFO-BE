package com.FA24SE088.OnlineForum.template;

public class EmailTemplate {
    public static String teamplateSendOtp( String otp){
        return "<!DOCTYPE html>\n" +
                "<html lang='en'>\n" +
                "<head>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                "    <title>Your OTP Code</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd;'>\n" +
                "        <h2>Your OTP Code</h2>\n" +
                "        <p>Hello,</p>\n" +
                "        <p>To complete your verification, please use the following OTP code:</p>\n" +
                "        <p style='font-size: 24px; font-weight: bold; color: #0056b3; text-align: left;'>["+ otp +"]</p>\n" +
                "        <p>This code is valid for 10 minutes. Please do not share it with anyone.</p>\n" +
                "        <p>Thank you for using our services!</p>\n" +
                "        <p>Best regards,<br>Your Company</p>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
}
