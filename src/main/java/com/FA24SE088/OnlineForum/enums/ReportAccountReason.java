package com.FA24SE088.OnlineForum.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum FeedbackReason {
    INVALID_ERROR_MESSAGE_KEY("Error Message Doesn't Match Any"),

    ;

    private String message;

    FeedbackReason(String message){
        this.message = message;
    }
}
