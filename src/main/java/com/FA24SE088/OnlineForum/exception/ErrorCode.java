package com.FA24SE088.OnlineForum.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    INVALID_ERROR_MESSAGE_KEY(10666, "Error Message Doesn't Match Any", HttpStatus.INTERNAL_SERVER_ERROR),
    UNDEFINED_EXCEPTION(10000, "Undefined Exception", HttpStatus.INTERNAL_SERVER_ERROR),
    EMPTY_LIST(10001, "List Doesn't Contain Any Information", HttpStatus.BAD_REQUEST),
    INVALID_PAGE_NUMBER(10002, "Page Number Must Be Greater Than 0", HttpStatus.BAD_REQUEST),
    INVALID_PER_PAGE_NUMBER(10003, "Per Page Number Must Be Greater Than 0", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(10004, "You Don't Have Permissions For This Function", HttpStatus.UNAUTHORIZED),
    AUTHORIZATION_DENIED_EXCEPTION(10005, "Your Role Cannot Access This Function", HttpStatus.FORBIDDEN),
    ACCOUNT_NOT_FOUND(10006, "Account Not Found", HttpStatus.NOT_FOUND),

    ACCOUNT_IS_EXISTED(10006, "Account is existed", HttpStatus.BAD_REQUEST),
    EMAIL_IS_EXISTED(10006, "Email is existed", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(10006, "Role Not Found", HttpStatus.NOT_FOUND),

    NAME_NOT_NULL(10007, "Name Cannot Be Null", HttpStatus.BAD_REQUEST),
    INVALID_URL(10008, "URL Must Be Valid", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_FOUND(10009, "Category Not Found", HttpStatus.NOT_FOUND),
    NAME_EXIST(10010, "Name Existed", HttpStatus.BAD_REQUEST)
    ;
    private int code;
    private String message;
    private HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode){
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
