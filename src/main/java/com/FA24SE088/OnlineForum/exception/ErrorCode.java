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
    DAILY_POINT_NOT_FOUND(10032, "Daily Point Not Found", HttpStatus.NOT_FOUND),
    WALLET_NOT_FOUND(10033, "Wallet Not Found", HttpStatus.NOT_FOUND),
    DAILY_POINT_ALREADY_EXIST(10034, "Daily Point Log Already Exist", HttpStatus.FOUND),
    MAX_POINT_LOWER_THAN_INDIVIDUAL_POINT(10035, "Max Point Must Be Greater Than Individual Point", HttpStatus.BAD_REQUEST),
    IMAGE_NOT_FOUND(10036, "Image Not Found", HttpStatus.NOT_FOUND),
    FEEDBACK_NOT_FOUND(10037, "Feedback Not Found", HttpStatus.NOT_FOUND),
    FEEDBACK_ALREADY_GOT_STATUS(10038, "Feedback Already Got This Status", HttpStatus.BAD_REQUEST),
    DOCUMENT_NOT_FOUND(10039, "Document Not Found", HttpStatus.NOT_FOUND),
    YOU_DO_NOT_HAVE_ENOUGH_POINT(10040, "You do not have enough points for this reward", HttpStatus.BAD_REQUEST),
    REWARD_HAS_BEEN_TAKEN(10041, "This reward has been taken", HttpStatus.BAD_REQUEST),
    UPVOTE_NOT_FOUND(10042, "Upvote Not Found", HttpStatus.NOT_FOUND),
    COMMENT_NOT_FOUND(10043, "Comment Not Found", HttpStatus.NOT_FOUND),
    ACCOUNT_COMMENT_NOT_MATCH(10044, "Account Does not Match Comment Owner", HttpStatus.BAD_REQUEST),
    WRONG_STATUS(10045, "wrong status", HttpStatus.BAD_REQUEST),
    REPORT_ACCOUNT_NOT_FOUND(10046, "Report not found", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_MATCH(10047, "Password not match", HttpStatus.BAD_REQUEST),
    DOCUMENT_HAS_BEEN_USED(10048, "There have been these users of document", HttpStatus.BAD_REQUEST),
    INVALID_HEX_FORMAT(10049, "Color Hex Must Be a Valid Hex Code", HttpStatus.BAD_REQUEST),
    DRAFT_POST_CANNOT_CHANGE_STATUS(10050, "Draft Post Status Cannot Be Changed", HttpStatus.BAD_REQUEST),
    COMPLETED_POST_CANNOT_BE_EDIT_IN_DRAFT(10051, "Completed Post Cannot Be Edited In Draft Section", HttpStatus.BAD_REQUEST),
    COMPLETED_POST_CANNOT_BE_UPDATE_TO_POST(10052, "Completed Post Cannot Be Updated To New Completed Post", HttpStatus.BAD_REQUEST),
    FOLLOW_NOT_FOUND(10053, "Follow not found", HttpStatus.NOT_FOUND),
    BLOCK_NOT_FOUND(10054, "Blocked not found", HttpStatus.NOT_FOUND),
    ACCOUNT_HAS_BEEN_FOLLOWED(10055, "This account has been followed", HttpStatus.BAD_REQUEST),
    CANNOT_FOLLOW_SELF(10056, "Cannot follow self", HttpStatus.BAD_REQUEST),
    CANNOT_UNFOLLOW_SELF(10057, "Cannot unfollow self", HttpStatus.BAD_REQUEST),
    CANNOT_BLOCK_SELF(10058, "Cannot block self", HttpStatus.BAD_REQUEST),
    CANNOT_UNBLOCK_SELF(10059, "Cannot unblock self", HttpStatus.BAD_REQUEST),
    BOOKMARK_ALREADY_EXISTS(10060, "Bookmark already exists", HttpStatus.BAD_REQUEST),
    BOOKMARK_NOT_FOUND(10061, "Bookmark not found", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(10062, "Unauthenticated", HttpStatus.BAD_REQUEST),
    POST_NOT_A_DRAFT(10063, "This Is A Post Not A Draft", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED(10063, "Otp expired", HttpStatus.BAD_REQUEST),
    OTP_NOT_FOUND(10064, "Otp not found", HttpStatus.NOT_FOUND),
    OTP_STILL_VALID(10065, "Otp still valid", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_FOUND(10066, "Email not found", HttpStatus.NOT_FOUND),
    ACCOUNT_WAS_ACTIVE(10067, "Account was active", HttpStatus.BAD_REQUEST),
    ACCOUNT_ALREADY_BLOCKED(10068, "This Account Is Already Blocked", HttpStatus.BAD_REQUEST),
    ACCOUNT_HAS_NOT_BEEN_AUTHENTICATED(10068, "The account has not been authenticated", HttpStatus.BAD_REQUEST),
    NOTIFICATION_NOT_FOUND(10066, "Notification not found", HttpStatus.NOT_FOUND),
    NULL_DRAFT(10065, "Draft Must Not Be Null", HttpStatus.BAD_REQUEST),
    ACCOUNT_NOT_THE_AUTHOR_OF_POST(10066, "This Account Is Not The Author Of This Post", HttpStatus.BAD_REQUEST),
    MISSING_REQUIRED_FIELDS_IN_DRAFT(10067, "Current Draft Is Missing Required Fields Like: 'Title, Content, Tag, Topic' To Convert To Completed Post", HttpStatus.BAD_REQUEST),
    TRANSACTION_NOT_FOUND(10068, "Transaction Not Found", HttpStatus.NOT_FOUND),
    INVALID_EMAIL(10069, "Invalid Email Format", HttpStatus.BAD_REQUEST),
    REQUEST_NULL(10068, "Request null", HttpStatus.NOT_FOUND),
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
