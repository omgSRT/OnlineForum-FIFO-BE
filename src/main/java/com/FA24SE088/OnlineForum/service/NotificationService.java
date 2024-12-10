package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.NotificationRequest;
import com.FA24SE088.OnlineForum.dto.response.NotificationResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Notification;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.NotificationMapper;
import com.FA24SE088.OnlineForum.repository.AccountRepository;
import com.FA24SE088.OnlineForum.repository.NotificationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class NotificationService {
    NotificationRepository notificationRepository;
    AccountRepository accountRepository;
    NotificationMapper notificationMapper;
    //DataHandler dataHandler;

    private Account getCurrentUser() {
        var context = SecurityContextHolder.getContext();
        return accountRepository.findByUsername(context.getAuthentication().getName())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    public NotificationResponse createNotification(NotificationRequest notificationRequest) {
        Account account = accountRepository.findById(notificationRequest.getAccountId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        Notification notification = notificationMapper.toNotification(notificationRequest);
        notification.setAccount(account);
        notification.setRead(false);
        notification.setCreatedDate(LocalDateTime.now());

        Notification savedNotification = notificationRepository.save(notification);
        NotificationResponse response = notificationMapper.toResponse(savedNotification);
        response.setAccount(account);
        return response;
    }

    public void sendPrivateNotification(NotificationRequest notificationRequest) {
        var savedData = saveNotification(notificationRequest);
        //dataHandler.sendToUser(notificationRequest.getAccountId(), savedData);
    }

    private Notification saveNotification(NotificationRequest notificationRequest) {

        Account account = accountRepository.findById(notificationRequest.getAccountId()).orElseThrow(
                () -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND)
        );
        Notification notification = Notification.builder()
                .title(notificationRequest.getTitle())
                .message(notificationRequest.getMessage())
                .isRead(false)
                .createdDate(LocalDateTime.now())
                .account(account)
                .build();
        return notificationRepository.save(notification);
    }

    public Optional<NotificationResponse> getNotificationById(UUID notificationId) {
        Optional<Notification> notificationOptional = notificationRepository.findById(notificationId);
        return notificationOptional.map(notificationMapper::toResponse);
    }

    public List<NotificationResponse> getAllNotificationsByAccount(UUID accountId) {
        Account account = accountRepository.findById(accountId).orElseThrow(
                () -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND)
        );
        List<Notification> notifications = notificationRepository
                .findByAccountOrderByCreatedDateDesc(account);
        return notifications.stream().map(notificationMapper::toResponse)
                .toList();
    }

    public List<NotificationResponse> getAllNotificationsOfThisAccount() {
        Account account = getCurrentUser();
        List<Notification> notifications = notificationRepository
                .findByAccountOrderByCreatedDateDesc(account);
        return notifications.stream()
                .map(notification -> {
                    NotificationResponse response = notificationMapper.toResponse(notification);
                    response.setAccount(account);
                    return response;
                })
                .toList();
    }

    public List<NotificationResponse> getAllNotifications() {
        List<Notification> notifications = notificationRepository.findAllByOrderByCreatedDateDesc();
        return notifications.stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    public void deleteNotification(UUID notificationId) {
        if (notificationRepository.existsById(notificationId)) {
            notificationRepository.deleteById(notificationId);
        } else {
            throw new AppException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }
    }
}

