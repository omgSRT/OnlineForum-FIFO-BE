package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.NotificationRequest;
import com.FA24SE088.OnlineForum.dto.response.NotificationResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Notification;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.NotificationMapper;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class NotificationService {
    UnitOfWork unitOfWork;
    NotificationMapper notificationMapper;

    private Account getCurrentUser(){
        var context = SecurityContextHolder.getContext();
        return unitOfWork.getAccountRepository().findByUsername(context.getAuthentication().getName())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    public NotificationResponse createNotification(NotificationRequest notificationRequest) {
        Account account = unitOfWork.getAccountRepository().findById(notificationRequest.getAccountId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        Notification notification = notificationMapper.toNotification(notificationRequest);
        notification.setAccount(account);
        notification.setRead(false);
        notification.setCreatedDate(new Date());

        Notification savedNotification = unitOfWork.getNotificationRepository().save(notification);
        return notificationMapper.toResponse(savedNotification);
    }

    public Optional<NotificationResponse> getNotificationById(UUID notificationId) {
        Optional<Notification> notificationOptional = unitOfWork.getNotificationRepository().findById(notificationId);
        return notificationOptional.map(notificationMapper::toResponse);
    }

    public List<NotificationResponse> getAllNotificationsOfThisAccount() {
        Account account = getCurrentUser();
        List<Notification> notifications = unitOfWork.getNotificationRepository().findByAccount(account);
        return notifications.stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    public List<NotificationResponse> getAllNotifications() {
        List<Notification> notifications = unitOfWork.getNotificationRepository().findAll();
        return notifications.stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    public void deleteNotification(UUID notificationId) {
        if (unitOfWork.getNotificationRepository().existsById(notificationId)) {
            unitOfWork.getNotificationRepository().deleteById(notificationId);
        } else {
            throw new AppException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }
    }
}

