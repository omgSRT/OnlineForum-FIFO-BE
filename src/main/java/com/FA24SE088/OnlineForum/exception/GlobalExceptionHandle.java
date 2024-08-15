package com.FA24SE088.OnlineForum.exception;

import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;

@ControllerAdvice
public class GlobalExceptionHandle {
    @ExceptionHandler(AppException.class)
    ResponseEntity<ApiResponse> handleAppException(AppException exception){
        ErrorCode error = exception.getErrorCode();
        ApiResponse apiRespone = new ApiResponse();

        apiRespone.setCode(error.getCode());
        apiRespone.setMessage(error.getMessage());

        return ResponseEntity.status(error.getStatusCode()).body(apiRespone);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiResponse> handleAccessDeniedException(AccessDeniedException exception){
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        return ResponseEntity.status(errorCode.getStatusCode()).body(
                ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception){
        String enumkey = exception.getFieldError().getDefaultMessage();
        ErrorCode errorCode = ErrorCode.INVALID_ERROR_MESSAGE_KEY;

        try {
            errorCode = ErrorCode.valueOf(enumkey);
        } catch (IllegalArgumentException e){}

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<ApiResponse> handleRuntimeException(IllegalStateException exception){
        ApiResponse response = new ApiResponse<>();
        response.setCode(ErrorCode.UNDEFINED_EXCEPTION.getCode());
        response.setMessage(ErrorCode.UNDEFINED_EXCEPTION.getMessage());
        System.out.println(exception.toString());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    ResponseEntity<ApiResponse> handleAuthorizationDeniedException(AuthorizationDeniedException exception){
        ApiResponse response = new ApiResponse<>();
        response.setCode(ErrorCode.AUTHORIZATION_DENIED_EXCEPTION.getCode());
        response.setMessage(ErrorCode.AUTHORIZATION_DENIED_EXCEPTION.getMessage());
        System.out.println(exception.toString());
        return ResponseEntity.badRequest().body(response);
    }
}
