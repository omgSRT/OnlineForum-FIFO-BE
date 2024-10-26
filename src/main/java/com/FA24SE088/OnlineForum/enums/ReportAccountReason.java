package com.FA24SE088.OnlineForum.enums;

import lombok.Getter;

@Getter
public enum ReportAccountReason {
    SPAMMING("Account is Spamming in Forum"),
    HARASSMENT("Account Engages in Harassment or Bullying"),
    INAPPROPRIATE_LANGUAGE("Account Uses Inappropriate Language"),
    FAKE_ACCOUNT("Account Appears to be Fake or Fraudulent"),
    IMPERSONATION("Account is Impersonating Someone Else"),
    OFFENSIVE_CONTENT("Account Shares Offensive or Harmful Content"),
    SELF_PROMOTION("Account Engages in Excessive Self-Promotion"),
    SCAM_ATTEMPT("Account Attempts to Scam or Defraud Others"),
    COPYRIGHT_VIOLATION("Account Shares Copyrighted Content Without Permission"),
    PRIVACY_VIOLATION("Account Violates Privacy of Others"),
    MALICIOUS_LINKS("Account Shares Malicious or Unsafe Links"),
    FALSE_INFORMATION("Account Spreads False or Misleading Information"),
    UNAUTHORIZED_ADVERTISING("Account Posts Unauthorized Advertising"),
    HATE_SPEECH("Account Engages in Hate Speech"),
    ILLEGAL_ACTIVITIES("Account Encourages or Engages in Illegal Activities");

    ;

    private String message;

    ReportAccountReason(String message){
        this.message = message;
    }
}
