package com.FA24SE088.OnlineForum.controller;

import com.FA24SE088.OnlineForum.dto.request.ReportRequest;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.ReportResponse;
import com.FA24SE088.OnlineForum.enums.FeedbackStatus;
import com.FA24SE088.OnlineForum.enums.SuccessReturnMessage;
import com.FA24SE088.OnlineForum.service.ReportService;
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
    final ReportService reportService;

    @Operation(summary = "Create New Feedback")
    @PostMapping("/create")
    public ApiResponse<ReportResponse> createFeedback(@RequestBody @Valid ReportRequest request){
        return reportService.createFeedback(request).thenApply(reportResponse ->
                ApiResponse.<ReportResponse>builder()
                        .message(SuccessReturnMessage.CREATE_SUCCESS.getMessage())
                        .entity(reportResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Update Feedback", description = "Change Feedback Status By ID")
    @PutMapping(path = "/update/{feedbackId}")
    public ApiResponse<ReportResponse> updateFeedbackStatusById(@PathVariable UUID feedbackId,
                                                                @RequestParam FeedbackStatus status){
        return reportService.updateFeedbackStatus(feedbackId, status).thenApply(reportResponse ->
                ApiResponse.<ReportResponse>builder()
                        .message(SuccessReturnMessage.UPDATE_SUCCESS.getMessage())
                        .entity(reportResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Delete Feedback", description = "Delete Feedback By ID")
    @DeleteMapping(path = "/delete/{feedbackId}")
    public ApiResponse<ReportResponse> deleteFeedbackById(@PathVariable UUID feedbackId){
        return reportService.deleteFeedbackById(feedbackId).thenApply(reportResponse ->
                ApiResponse.<ReportResponse>builder()
                        .message(SuccessReturnMessage.DELETE_SUCCESS.getMessage())
                        .entity(reportResponse)
                        .build()
                ).join();
    }

    @Operation(summary = "Get All Feedbacks")
    @GetMapping(path = "/getall")
    public ApiResponse<List<ReportResponse>> getAllFeedbacks(@RequestParam(defaultValue = "1") int page,
                                                             @RequestParam(defaultValue = "10") int perPage,
                                                             @RequestParam(required = false) UUID postId){
        return reportService.getAllFeedback(page, perPage, postId).thenApply(feedbackResponses ->
                ApiResponse.<List<ReportResponse>>builder()
                        .entity(feedbackResponses)
                        .build()
                ).join();
    }

    @Operation(summary = "Get Feedback", description = "Get Feedback By ID")
    @GetMapping(path = "/get/{feedbackId}")
    public ApiResponse<ReportResponse> getFeedbackById(@PathVariable UUID feedbackId){
        return reportService.getFeedbackById(feedbackId).thenApply(reportResponse ->
                ApiResponse.<ReportResponse>builder()
                        .entity(reportResponse)
                        .build()
        ).join();
    }
}
