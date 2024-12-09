package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.*;
import com.FA24SE088.OnlineForum.dto.response.FeedbackResponse;
import com.FA24SE088.OnlineForum.entity.*;
import com.FA24SE088.OnlineForum.enums.FeedbackStatus;
import com.FA24SE088.OnlineForum.enums.FeedbackUpdateStatus;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.FeedbackMapper;
import com.FA24SE088.OnlineForum.repository.AccountRepository;
import com.FA24SE088.OnlineForum.repository.FeedbackRepository;
import com.FA24SE088.OnlineForum.repository.NotificationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class FeedbackService {
    AccountRepository accountRepository;
    FeedbackRepository feedbackRepository;
    NotificationRepository notificationRepository;
    FeedbackMapper feedbackMapper;

    private Account getCurrentUser() {
        var context = SecurityContextHolder.getContext();
        return accountRepository
                .findByUsername(context.getAuthentication().getName()).orElseThrow(()
                        -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    public FeedbackResponse createFeedback(FeedbackRequest feedbackRequest) {
        Account account = getCurrentUser();
        if (!account.getRole().getName().equals("USER")) {
            throw new AppException(ErrorCode.FEEDBACK_JUST_FOR_USER);
        }
        Feedback feedback = feedbackMapper.toFeedback(feedbackRequest);
        feedback.setAccount(account);
        feedback.setStatus(FeedbackStatus.PENDING.name());
        Feedback savedFeedback = feedbackRepository.save(feedback);
        return feedbackMapper.toResponse(savedFeedback);
    }


    public Optional<FeedbackResponse> updateFeedback(UUID feedbackId, FeedbackUpdateStatus status) {
        Optional<Feedback> feedbackOptional = feedbackRepository.findById(feedbackId);
        if (feedbackOptional.isPresent()) {
            Feedback feedback = feedbackOptional.get();
            if (!feedback.getStatus().equalsIgnoreCase(FeedbackStatus.PENDING.name())) {
                throw new AppException(ErrorCode.WRONG_STATUS);
            }
            feedback.setStatus(status.name());
            Feedback updatedFeedback = feedbackRepository.save(feedback);

            Notification notification = Notification.builder()
                    .title("Feedback Noitfication")
                    .message("Your feedback send success!")
                    .isRead(false)
                    .account(feedback.getAccount())
                    .createdDate(feedback.getCreatedDate())
                    .build();
            notificationRepository.save(notification);

            return Optional.of(feedbackMapper.toResponse(updatedFeedback));
        }
        return Optional.empty();
    }

    public Optional<FeedbackResponse> getFeedbackById(UUID feedbackId) {
        Optional<Feedback> feedbackOptional = feedbackRepository.findById(feedbackId);
        return feedbackOptional.map(feedbackMapper::toResponse);
    }

    public List<FeedbackResponse> getAllFeedbacks() {
        List<Feedback> feedbacks = feedbackRepository.findAll();
        return feedbacks.stream()
                .map(feedbackMapper::toResponse)
                .toList();
    }

    public List<FeedbackResponse> filter(UUID id, String username, FeedbackStatus status, boolean ascending) {
        List<FeedbackResponse> list = new ArrayList<>(feedbackRepository.findAll().stream()
                .filter(feedback -> (id == null || (feedback.getAccount() != null && feedback.getAccount().getAccountId() != null && feedback.getAccount().getAccountId().equals(id))))
                .filter(feedback -> (username == null ||
                        (feedback.getAccount().getUsername() != null && feedback.getAccount().getUsername().contains(username))))
                .filter(feedback -> (status == null || (feedback.getStatus() != null && feedback.getStatus().contains(status.name()))))
                .map(feedbackMapper::toResponse)
                .toList());
        list.sort((f1, f2) -> {
            if (ascending) {
                return f1.getCreatedDate().compareTo(f2.getCreatedDate());
            } else {
                return f2.getCreatedDate().compareTo(f1.getCreatedDate());
            }
        });

        return list;
    }

    public void deleteFeedback(UUID feedbackId) {
        if (feedbackRepository.existsById(feedbackId)) {
            feedbackRepository.deleteById(feedbackId);
        } else {
            throw new AppException(ErrorCode.FEEDBACK_NOT_FOUND);
        }
    }
}

