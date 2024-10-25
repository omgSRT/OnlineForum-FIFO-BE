package com.FA24SE088.OnlineForum.controller;


import com.FA24SE088.OnlineForum.dto.request.NotificationRequest;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.NotificationResponse;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/notification")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class NotificationController {
    final NotificationService notificationService;

    @Operation(summary = "Create notification", description = "Creates a new notification for the current user")
    @PostMapping("/create")
    public ApiResponse<NotificationResponse> createNotification(@RequestBody NotificationRequest notificationRequest) {
        return ApiResponse.<NotificationResponse>builder()
                .entity(notificationService.createNotification(notificationRequest))
                .build();
    }

    @GetMapping("/get-by-id/{id}")
    public ApiResponse<NotificationResponse> getNotification(@PathVariable UUID id) {
        return ApiResponse.<NotificationResponse>builder()
                .entity(notificationService.getNotificationById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND)))
                .build();
    }

    @GetMapping("/get-all")
    public ApiResponse<List<NotificationResponse>> getAllNotifications() {
        return ApiResponse.<List<NotificationResponse>>builder()
                .entity(notificationService.getAllNotifications())
                .build();
    }

    @GetMapping("/get-my-notifications")
    public ApiResponse<List<NotificationResponse>> getMyNotifications() {
        return ApiResponse.<List<NotificationResponse>>builder()
                .entity(notificationService.getAllNotificationsOfThisAccount())
                .build();
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> deleteNotification(@PathVariable UUID id) {
        notificationService.deleteNotification(id);
        return ApiResponse.<Void>builder().build();
    }
}

