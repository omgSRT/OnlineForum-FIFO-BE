package com.FA24SE088.OnlineForum.controller;

import com.FA24SE088.OnlineForum.dto.request.ReportRequest;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.ReportResponse;
import com.FA24SE088.OnlineForum.enums.ReportStatus;
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
@RequestMapping("/report")
@Slf4j
public class ReportController {
    final ReportService reportService;

    @Operation(summary = "Create New Post Report")
    @PostMapping("/create")
    public ApiResponse<ReportResponse> createReport(@RequestBody @Valid ReportRequest request){
        return reportService.createReport(request).thenApply(reportResponse ->
                ApiResponse.<ReportResponse>builder()
                        .message(SuccessReturnMessage.CREATE_SUCCESS.getMessage())
                        .entity(reportResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Update Report", description = "Change Report Status By ID")
    @PutMapping(path = "/update/{reportId}")
    public ApiResponse<ReportResponse> updateReportStatusById(@PathVariable UUID reportId,
                                                                @RequestParam ReportStatus status){
        return reportService.updateReportStatus(reportId, status).thenApply(reportResponse ->
                ApiResponse.<ReportResponse>builder()
                        .message(SuccessReturnMessage.UPDATE_SUCCESS.getMessage())
                        .entity(reportResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Delete Report", description = "Delete Report By ID")
    @DeleteMapping(path = "/delete/{reportId}")
    public ApiResponse<ReportResponse> deleteReportById(@PathVariable UUID reportId){
        return reportService.deleteReportById(reportId).thenApply(reportResponse ->
                ApiResponse.<ReportResponse>builder()
                        .message(SuccessReturnMessage.DELETE_SUCCESS.getMessage())
                        .entity(reportResponse)
                        .build()
                ).join();
    }

    @Operation(summary = "Get All Reports")
    @GetMapping(path = "/getall")
    public ApiResponse<List<ReportResponse>> getAllFeedbacks(@RequestParam(defaultValue = "1") int page,
                                                             @RequestParam(defaultValue = "10") int perPage,
                                                             @RequestParam(required = false) UUID postId){
        return reportService.getAllReports(page, perPage, postId).thenApply(reportResponses ->
                ApiResponse.<List<ReportResponse>>builder()
                        .entity(reportResponses)
                        .build()
                ).join();
    }

    @Operation(summary = "Get Report", description = "Get Report By ID")
    @GetMapping(path = "/get/{reportId}")
    public ApiResponse<ReportResponse> getReportById(@PathVariable UUID reportId){
        return reportService.getReportById(reportId).thenApply(reportResponse ->
                ApiResponse.<ReportResponse>builder()
                        .entity(reportResponse)
                        .build()
        ).join();
    }
}
