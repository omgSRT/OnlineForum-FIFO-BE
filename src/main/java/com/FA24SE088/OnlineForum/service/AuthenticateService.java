package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.AccountChangePasswordRequest;
import com.FA24SE088.OnlineForum.dto.request.AuthenticationRequest;
import com.FA24SE088.OnlineForum.dto.request.IntrospectRequest;
import com.FA24SE088.OnlineForum.dto.request.LogoutRequest;
import com.FA24SE088.OnlineForum.dto.response.AccountResponse;
import com.FA24SE088.OnlineForum.dto.response.AuthenticationResponse;
import com.FA24SE088.OnlineForum.dto.response.IntrospectResponse;
import com.FA24SE088.OnlineForum.dto.response.RefreshAccessTokenResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.InvalidatedToken;
import com.FA24SE088.OnlineForum.enums.AccountStatus;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;

import com.FA24SE088.OnlineForum.mapper.AccountMapper;
import com.FA24SE088.OnlineForum.repository.AccountRepository;
import com.FA24SE088.OnlineForum.repository.InvalidateTokenRepository;
import com.FA24SE088.OnlineForum.utils.EmailUtil;
import com.FA24SE088.OnlineForum.utils.OtpUtil;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@Service
public class AuthenticateService {
    final AccountRepository accountRepository;
    final InvalidateTokenRepository invalidateTokenRepository;
    final EmailUtil emailUtil;
    final AccountMapper accountMapper;
    final OtpUtil otpUtil;
    @Value("${spring.custom.jwt.secret}")
    String jwtSecret;

    public IntrospectResponse introspectJWT(IntrospectRequest request) throws JOSEException, ParseException {
        String token = request.getToken();
        boolean invalid = true;
        try {
            verifyToken(token);
        } catch (RuntimeException e) {
            invalid = false;
        }
        //Check And Return Bool If  And JWT Expired
        return IntrospectResponse
                .builder()
                .valid(invalid)
                .build();
    }

    public AuthenticationResponse authenticated(AuthenticationRequest request) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        var account = accountRepository.findByUsername(request.getUsername()).orElseThrow(
                () -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND)
        );
        boolean authenticated = passwordEncoder.matches(request.getPassword(), account.getPassword());
        if (!authenticated) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
//        if(account.getStatus().equals(AccountStatus.BANED.name())){
//            long daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), account.getBannedUntil()) + 1;
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your account is also banned in " + daysRemaining + " days.");
//        }
        if (account.getStatus().equals(AccountStatus.PENDING_APPROVAL.name())) {
            throw new AppException(ErrorCode.ACCOUNT_HAS_NOT_BEEN_AUTHENTICATED);
        }
        if (account.getStatus().equals(AccountStatus.INACTIVE.name())) {
            throw new AppException(ErrorCode.ACCOUNT_INACTIVE);
        }
        var token = generateToken(account, 1);
        var refreshToken = generateToken(account, 365);
        return AuthenticationResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .authenticated(true)
                .build();
    }

    public String generateToken(Account account, int expirationDay) {
        Date now = new Date();
        Instant nowInstant = now.toInstant();
        Instant expirationInstant = nowInstant.plus(expirationDay, ChronoUnit.DAYS);
        Date expirationTime = Date.from(expirationInstant);

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(account.getUsername())
                .issuer("dev-GSU24SE23")
                .issueTime(now)
                .expirationTime(expirationTime)
                .jwtID(UUID.randomUUID().toString())
                .claim("accountId", account.getAccountId())
                .claim("scope", buildScope(account))
                .claim("username", account.getUsername())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(jwtSecret.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot Create JWT", e);
            throw new RuntimeException(e);
        }
    }

    private boolean verifyRefreshToken(String refreshToken, Account account) {
        try {
            JWSObject jwsObject = JWSObject.parse(refreshToken);
            if (!jwsObject.verify(new MACVerifier(jwtSecret.getBytes()))) {
                return false; // Signature verification failed
            }

            JWTClaimsSet claimsSet = JWTClaimsSet.parse(jwsObject.getPayload().toJSONObject());
            if (claimsSet.getExpirationTime().before(new Date())) {
                return false; // Token has expired
            }

            String accountIdClaim = (String) claimsSet.getClaim("accountId");
            if (!account.getAccountId().equals(UUID.fromString(accountIdClaim))) {
                return false; // Account ID mismatch
            }

            return true; // Refresh token is valid
        } catch (ParseException | JOSEException e) {
            log.error("Token validation failed", e);
            return false; // Token validation failed
        }
    }

    public RefreshAccessTokenResponse generateNewAccessTokenFromRefreshToken(String refreshToken, String username) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (!verifyRefreshToken(refreshToken, account)) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        var newAccessToken = generateToken(account, 1);

        return RefreshAccessTokenResponse.builder()
                .accessToken(newAccessToken)
                .build();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        var signToken = verifyToken(request.getToken());

        String jti = signToken.getJWTClaimsSet().getJWTID();
        Date expTime = signToken.getJWTClaimsSet().getExpirationTime();
        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jti)
                .expiryTime(expTime)
                .build();

        invalidateTokenRepository.save(invalidatedToken);
    }

    public CompletableFuture<Void> forgetPassword(String email) {
        var foundAccountFuture = accountRepository.findByEmailIgnoreCase(email)
                .thenApply(optionalAccount -> optionalAccount.orElseThrow(() ->
                        new AppException(ErrorCode.ACCOUNT_NOT_FOUND)));

        return foundAccountFuture.thenCompose(account -> {
//            String emailBody = "<html>"
//                    + "<body>"
//                    + "<p><strong>FIFO Password Reset</strong></p>"
//                    + "<p>We heard that you lost your FIFO password. Sorry about that!</p>"
//                    + "<p>But don’t worry! You can use the following button to reset your password:</p>"
//                    + "<a href=\"https://your-reset-url.com/reset?email=" + account.getEmail() + "\" "
//                    + "style=\"display: inline-block; padding: 10px 20px; font-size: 16px; color: white; "
//                    + "background-color: #4CAF50; text-decoration: none; border-radius: 5px;\">"
//                    + "Reset Password"
//                    + "</a>"
//                    + "</body>"
//                    + "</html>";
            String emailBody = "<html>"
                    + "<body>"
                    + "<p><strong>FIFO Password Reset</strong></p>"
                    + "<p>We heard that you lost your FIFO password. Sorry about that!</p>"
                    + "<p>Don't worry! Enter This OTP To Reset Your Password: " + otpUtil.generateOtp(account.getEmail()).getOtpEmail() + " </p>"
                    + "</body>"
                    + "</html>";

            emailUtil.sendToAnEmailWithHTMLEnabled(account.getEmail(),
                    emailBody,
                    "Please Reset Your Password");

            return CompletableFuture.completedFuture(null);
        });
    }

    public CompletableFuture<AccountResponse> changePassword(String email, AccountChangePasswordRequest request) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        var foundAccountFuture = accountRepository.findByEmailIgnoreCase(email)
                .thenApply(optionalAccount -> optionalAccount.orElseThrow(() ->
                        new AppException(ErrorCode.ACCOUNT_NOT_FOUND)));

        return foundAccountFuture.thenCompose(account -> {
                    if (!request.getPassword().equals(request.getConfirmPassword())) {
                        throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
                    }

                    account.setPassword(passwordEncoder.encode(request.getPassword()));

                    return CompletableFuture.completedFuture(accountRepository.save(account));
                })
                .thenApply(accountMapper::toResponse);
    }

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(jwtSecret.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiredDate = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);
        if (!(verified && expiredDate.after(new Date()))) {
            throw new RuntimeException("String.valueOf(ErrorCode.UNAUTHENTICATED)");
        }
        if (invalidateTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new RuntimeException("unauthenticated");
        }

        return signedJWT;
    }

    private String buildScope(Account account) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        // Kiểm tra xem role của tài khoản có khác null không
        if (account.getRole() != null && account.getRole().getName() != null) {
            stringJoiner.add(account.getRole().getName());
        }
        return stringJoiner.toString();
    }
}
