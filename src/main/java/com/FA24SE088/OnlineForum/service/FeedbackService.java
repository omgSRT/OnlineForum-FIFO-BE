package com.FA24SE088.OnlineForum.service;


import com.FA24SE088.OnlineForum.dto.request.AccountRequest;
import com.FA24SE088.OnlineForum.dto.request.AccountUpdateCategoryRequest;
import com.FA24SE088.OnlineForum.dto.request.AccountUpdateRequest;
import com.FA24SE088.OnlineForum.dto.request.FeedbackRequest;
import com.FA24SE088.OnlineForum.dto.response.AccountResponse;
import com.FA24SE088.OnlineForum.dto.response.FeedbackResponse;
import com.FA24SE088.OnlineForum.entity.*;
import com.FA24SE088.OnlineForum.enums.AccountStatus;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.AccountMapper;
import com.FA24SE088.OnlineForum.mapper.FeedbackMapper;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import com.FA24SE088.OnlineForum.utils.PaginationUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@Service
public class FeedbackService {

    @Autowired
    UnitOfWork unitOfWork;

    @Autowired
    private FeedbackMapper feedbackMapper;

    public FeedbackResponse createFeedback(FeedbackRequest feedbackRequest) {
        Account account = unitOfWork.getAccountRepository().findById(feedbackRequest.getAccountId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        Feedback feedback = feedbackMapper.toFeedback(feedbackRequest);
        feedback.setAccount(account);
        Feedback savedFeedback = unitOfWork.getFeedbackRepository().save(feedback);
        return feedbackMapper.toResponse(savedFeedback);
    }

    public Optional<FeedbackResponse> updateFeedback(UUID feedbackId, FeedbackRequest feedbackRequest) {
        Optional<Feedback> feedbackOptional = unitOfWork.getFeedbackRepository().findById(feedbackId);
        if (feedbackOptional.isPresent()) {
            Feedback feedback = feedbackOptional.get();
            feedback.setTitle(feedbackRequest.getTitle());
            feedback.setContent(feedbackRequest.getContent());
            feedback.setStatus(feedbackRequest.getStatus());

            Account account = unitOfWork.getAccountRepository().findById(feedbackRequest.getAccountId())
                    .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
            feedback.setAccount(account);

            Feedback updatedFeedback = unitOfWork.getFeedbackRepository().save(feedback);
            return Optional.of(feedbackMapper.toResponse(updatedFeedback));
        }
        return Optional.empty();
    }

    public Optional<FeedbackResponse> getFeedbackById(UUID feedbackId) {
        Optional<Feedback> feedbackOptional = unitOfWork.getFeedbackRepository().findById(feedbackId);
        return feedbackOptional.map(feedbackMapper::toResponse);
    }

    public List<FeedbackResponse> getAllFeedbacks() {
        List<Feedback> feedbacks = unitOfWork.getFeedbackRepository().findAll();
        return feedbacks.stream()
                .map(feedbackMapper::toResponse)
                .collect(Collectors.toList());
    }

    public void deleteFeedback(UUID feedbackId) {
        if (unitOfWork.getFeedbackRepository().existsById(feedbackId)) {
            unitOfWork.getFeedbackRepository().deleteById(feedbackId);
        }else {
            throw new AppException(ErrorCode.FEEDBACK_NOT_FOUND);
        }
    }
}

