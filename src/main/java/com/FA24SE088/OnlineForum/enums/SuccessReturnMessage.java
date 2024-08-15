package com.FA24SE088.OnlineForum.enums;

import lombok.Getter;

@Getter
public enum SuccessReturnMessage {
    SEARCH_SUCCESS("Search Successfully"),
    CREATE_SUCCESS("Create Successfully"),
    UPDATE_SUCCESS("Update Successfully"),
    CHANGE_SUCCESS("Change Successfully"),
    DELETE_SUCCESS("Delete Successfully"),
    CONVERT_SUCCESS("Convert Successfully"),
    SEND_SUCCESS("Send Successfully"),
    POST_SUCCESS("Post Successfully"),
    LOGIN_SUCCESS("Login Successfully"),
    ;

    private String message;
    SuccessReturnMessage(String message){
        this.message = message;
    }
}
