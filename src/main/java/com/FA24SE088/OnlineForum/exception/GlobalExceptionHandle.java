package com.FA24SE088.OnlineForum.exception;

import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.ResponseObject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandle {
    @ExceptionHandler(AppException.class)
    ResponseEntity<ApiResponse> handleAppException(AppException exception) {
        ErrorCode error = exception.getErrorCode();
        ApiResponse apiRespone = new ApiResponse();

        apiRespone.setCode(error.getCode());
        apiRespone.setMessage(error.getMessage());

        return ResponseEntity.status(error.getStatusCode()).body(apiRespone);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiResponse> handleAccessDeniedException(AccessDeniedException exception) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        return ResponseEntity.status(errorCode.getStatusCode()).body(
                ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public final ResponseObject<?> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, Object> details = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(violation ->
                        violation.getPropertyPath().toString(), ConstraintViolation::getMessage));
        return new ResponseObject<>(HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_PARAMETER.getCode() +
                        ErrorCode.INVALID_PARAMETER.getMessage(), details);
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<ApiResponse> handleRuntimeException(IllegalStateException exception) {
        ApiResponse response = new ApiResponse<>();
        response.setCode(ErrorCode.UNDEFINED_EXCEPTION.getCode());
        response.setMessage(ErrorCode.UNDEFINED_EXCEPTION.getMessage());
        System.out.println(exception.toString());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    ResponseEntity<ApiResponse> handleAuthorizationDeniedException(AuthorizationDeniedException exception) {
        ApiResponse response = new ApiResponse<>();
        response.setCode(ErrorCode.AUTHORIZATION_DENIED_EXCEPTION.getCode());
        response.setMessage(ErrorCode.AUTHORIZATION_DENIED_EXCEPTION.getMessage());
        System.out.println(exception.toString());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

}
