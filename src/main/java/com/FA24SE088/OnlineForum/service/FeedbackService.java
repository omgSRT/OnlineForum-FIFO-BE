package com.FA24SE088.OnlineForum.service;


import com.FA24SE088.OnlineForum.dto.request.*;
import com.FA24SE088.OnlineForum.dto.response.FeedbackResponse;
import com.FA24SE088.OnlineForum.entity.*;
import com.FA24SE088.OnlineForum.enums.FeedbackStatus;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.FeedbackMapper;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class FeedbackService {
    UnitOfWork unitOfWork;
    FeedbackMapper feedbackMapper;

    private Account getCurrentUser(){
        var context = SecurityContextHolder.getContext();
        return unitOfWork.getAccountRepository().findByUsername(context.getAuthentication().getName()).orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }
    public FeedbackResponse createFeedback(FeedbackRequest feedbackRequest) {
        Account account = getCurrentUser();
        Feedback feedback = feedbackMapper.toFeedback(feedbackRequest);
        feedback.setAccount(account);
        feedback.setStatus(FeedbackStatus.PENDING.name());
        log.info(account.getAccountId().toString());
        Feedback savedFeedback = unitOfWork.getFeedbackRepository().save(feedback);
        return feedbackMapper.toResponse(savedFeedback);
    }

    public Optional<FeedbackResponse> updateFeedback(UUID feedbackId, FeedbackRequest2 feedbackRequest) {
        Optional<Feedback> feedbackOptional = unitOfWork.getFeedbackRepository().findById(feedbackId);
        if(!feedbackRequest.getStatus().equals(FeedbackStatus.PENDING.name()) &&
                !feedbackRequest.getStatus().equals(FeedbackStatus.APPROVED.name())){
            throw new AppException(ErrorCode.WRONG_STATUS);
        }
        if (feedbackOptional.isPresent()) {
            Feedback feedback = feedbackOptional.get();
            feedback.setStatus(feedbackRequest.getStatus());
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
                .toList();
    }

    public void deleteFeedback(UUID feedbackId) {
        if (unitOfWork.getFeedbackRepository().existsById(feedbackId)) {
            unitOfWork.getFeedbackRepository().deleteById(feedbackId);
        }else {
            throw new AppException(ErrorCode.FEEDBACK_NOT_FOUND);
        }
    }
}

