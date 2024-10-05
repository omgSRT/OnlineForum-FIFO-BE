package com.FA24SE088.OnlineForum.exception;

import com.google.api.Http;
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
    ACCOUNT_IS_EXISTED(10007, "Account is existed", HttpStatus.BAD_REQUEST),
    EMAIL_IS_EXISTED(10008, "Email is existed", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(10009, "Role Not Found", HttpStatus.NOT_FOUND),
    NAME_NOT_NULL(10010, "Name Cannot Be Null", HttpStatus.BAD_REQUEST),
    INVALID_URL(10011, "URL Must Be Valid", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_FOUND(10012, "Category Not Found", HttpStatus.NOT_FOUND),
    NAME_EXIST(10013, "Name Existed", HttpStatus.BAD_REQUEST),
    WRONG_OTP(10014, "wrong otp", HttpStatus.BAD_REQUEST),
    WALLET_IS_EXISTED(10015, "wallet is existed in this account", HttpStatus.BAD_REQUEST),
    WALLET_NOT_EXIST(10016, "wallet is not exist in this account", HttpStatus.BAD_REQUEST),
    TOPIC_NOT_FOUND(10017, "Topic Not Found", HttpStatus.NOT_FOUND),
    TAG_NOT_FOUND(10018, "Tag Not Found", HttpStatus.NOT_FOUND),
    EMAIL_CONTENT_BLANK(10019, "Email Content Cannot Be Blank", HttpStatus.BAD_REQUEST),
    SEND_MAIL_FAILED(10020, "Failed To Send Email To Participants", HttpStatus.EXPECTATION_FAILED),
    TO_EMAIL_EMPTY(10021, "No Send To Emails Found", HttpStatus.NOT_FOUND),
    CATEGORY_HAS_UNDERTAKE(10022, "This category has someone to undertake", HttpStatus.BAD_REQUEST),
    MAX_POINT_LOWER_THAN_ONE(10023, "Max point must be greater than or equal to 1", HttpStatus.BAD_REQUEST),
    POINT_PER_POST_LOWER_THAN_ONE(10024, "Point per post must be greater than or equal to 1", HttpStatus.BAD_REQUEST),
    POINT_DATA_EXIST(10025, "Point Data Already Exist", HttpStatus.CREATED),
    POINT_NOT_FOUND(10026, "Point Not Found", HttpStatus.NOT_FOUND),
    TITLE_NULL(10027, "Title Cannot Be Null", HttpStatus.BAD_REQUEST),
    CONTENT_NULL(10028, "Content Cannot Be Null", HttpStatus.BAD_REQUEST),
    URL_NULL(10029, "URL Cannot Be Null", HttpStatus.BAD_REQUEST),
    POINT_EARNED_LOWER_THAN_ZERO(10030, "Point earned must be greater than or equal to 0", HttpStatus.BAD_REQUEST),
    POST_NOT_FOUND(10031, "Post Not Found", HttpStatus.NOT_FOUND),
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
