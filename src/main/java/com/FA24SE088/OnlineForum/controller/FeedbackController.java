package com.FA24SE088.OnlineForum.controller;


import com.FA24SE088.OnlineForum.dto.request.FeedbackRequest;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.FeedbackResponse;
import com.FA24SE088.OnlineForum.enums.FeedbackStatus;
import com.FA24SE088.OnlineForum.enums.FeedbackUpdateStatus;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;

import com.FA24SE088.OnlineForum.service.FeedbackService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/feedback")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class FeedbackController {
    FeedbackService feedbackService;

    @Operation(summary = "Create feedback", description = "Status: APPROVED,REJECTED")
    @PostMapping("/create")
    public ApiResponse<FeedbackResponse> createFeedback(@RequestBody @Validated FeedbackRequest feedbackRequest) {
        return ApiResponse.<FeedbackResponse>builder()
                .entity(feedbackService.createFeedback(feedbackRequest))
                .build();
    }

    @Operation(summary = "Update feedback", description = "Status: APPROVED,REJECTED")
    @PutMapping("/update/{id}")
    public ApiResponse<FeedbackResponse> updateFeedback(@PathVariable UUID id,
                                                        @RequestParam(defaultValue = "REJECTED") FeedbackUpdateStatus status) throws JsonProcessingException {
        return ApiResponse.<FeedbackResponse>builder()
                .entity(feedbackService.updateFeedback(id, status)
                        .orElseThrow(() -> new AppException(ErrorCode.FEEDBACK_NOT_FOUND)))
                .build();
    }

    @GetMapping("/filter")
    public ApiResponse<List<FeedbackResponse>> filter(
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false, defaultValue = "PENDING") FeedbackStatus status,
            @RequestParam(defaultValue = "true") boolean acsending) {
        return ApiResponse.<List<FeedbackResponse>>builder()
                .entity(feedbackService.filter(accountId, username, status, acsending))
                .build();
    }


    @GetMapping("/get-by-id/{id}")
    public ApiResponse<FeedbackResponse> getFeedback(@PathVariable UUID id) {
        return ApiResponse.<FeedbackResponse>builder()
                .entity(feedbackService.getFeedbackById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.FEEDBACK_NOT_FOUND)))
                .build();
    }

    @GetMapping("/get-all")
    public ApiResponse<List<FeedbackResponse>> getAllFeedbacks() {
        return ApiResponse.<List<FeedbackResponse>>builder()
                .entity(feedbackService.getAllFeedbacks())
                .build();
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> deleteFeedback(@PathVariable UUID id) {
        feedbackService.deleteFeedback(id);
        return ApiResponse.<Void>builder().build();
    }


}
