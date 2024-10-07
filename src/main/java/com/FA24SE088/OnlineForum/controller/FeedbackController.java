package com.FA24SE088.OnlineForum.controller;

import com.FA24SE088.OnlineForum.dto.request.FeedbackRequest;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.FeedbackResponse;
import com.FA24SE088.OnlineForum.enums.FeedbackStatus;
import com.FA24SE088.OnlineForum.enums.SuccessReturnMessage;
import com.FA24SE088.OnlineForum.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/feedback")
@Slf4j
public class FeedbackController {
    final FeedbackService feedbackService;

    @Operation(summary = "Create New Feedback")
    @PostMapping("/create")
    public ApiResponse<FeedbackResponse> createFeedback(@RequestBody @Valid FeedbackRequest request){
        return feedbackService.createFeedback(request).thenApply(feedbackResponse ->
                ApiResponse.<FeedbackResponse>builder()
                        .message(SuccessReturnMessage.CREATE_SUCCESS.getMessage())
                        .entity(feedbackResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Update Feedback", description = "Change Feedback Status By ID")
    @PutMapping(path = "/update/{feedbackId}")
    public ApiResponse<FeedbackResponse> updateFeedbackStatusById(@PathVariable UUID feedbackId,
                                                            @RequestParam FeedbackStatus status){
        return feedbackService.updateFeedbackStatus(feedbackId, status).thenApply(feedbackResponse ->
                ApiResponse.<FeedbackResponse>builder()
                        .message(SuccessReturnMessage.UPDATE_SUCCESS.getMessage())
                        .entity(feedbackResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Delete Feedback", description = "Delete Feedback By ID")
    @DeleteMapping(path = "/delete/{feedbackId}")
    public ApiResponse<FeedbackResponse> deleteFeedbackById(@PathVariable UUID feedbackId){
        return feedbackService.deleteFeedbackById(feedbackId).thenApply(feedbackResponse ->
                ApiResponse.<FeedbackResponse>builder()
                        .message(SuccessReturnMessage.DELETE_SUCCESS.getMessage())
                        .entity(feedbackResponse)
                        .build()
                ).join();
    }

    @Operation(summary = "Get All Feedbacks")
    @GetMapping(path = "/getall")
    public ApiResponse<List<FeedbackResponse>> getAllFeedbacks(@RequestParam(defaultValue = "1") int page,
                                                               @RequestParam(defaultValue = "10") int perPage,
                                                               @RequestParam(required = false) UUID postId){
        return feedbackService.getAllFeedback(page, perPage, postId).thenApply(feedbackResponses ->
                ApiResponse.<List<FeedbackResponse>>builder()
                        .entity(feedbackResponses)
                        .build()
                ).join();
    }
}
