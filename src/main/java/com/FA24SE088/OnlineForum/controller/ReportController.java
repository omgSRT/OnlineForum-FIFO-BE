package com.FA24SE088.OnlineForum.controller;

import com.FA24SE088.OnlineForum.dto.request.ReportRequest;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.ReportResponse;
import com.FA24SE088.OnlineForum.enums.ReportPostReason;
import com.FA24SE088.OnlineForum.enums.ReportPostStatus;
import com.FA24SE088.OnlineForum.enums.ReportPostUpdateStatus;
import com.FA24SE088.OnlineForum.enums.SuccessReturnMessage;
import com.FA24SE088.OnlineForum.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/post-report")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReportController {
    ReportService reportService;

    @Operation(summary = "Create New Post Report")
    @PostMapping("/create")
    public ApiResponse<ReportResponse> createReport(@RequestBody @Valid ReportRequest request,
                                                    @RequestParam ReportPostReason reportReason) {
        return reportService.createReport(request, reportReason).thenApply(reportResponse ->
                ApiResponse.<ReportResponse>builder()
                        .message(SuccessReturnMessage.CREATE_SUCCESS.getMessage())
                        .entity(reportResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Delete Report", description = "Delete Post Report By ID")
    @DeleteMapping(path = "/delete/{reportId}")
    public ApiResponse<ReportResponse> deleteReportById(@PathVariable UUID reportId) {
        return reportService.deleteReportById(reportId).thenApply(reportResponse ->
                ApiResponse.<ReportResponse>builder()
                        .message(SuccessReturnMessage.DELETE_SUCCESS.getMessage())
                        .entity(reportResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Get All Post Reports")
    @GetMapping(path = "/getall")
    public ApiResponse<List<ReportResponse>> getAllReports(@RequestParam(defaultValue = "1") int page,
                                                           @RequestParam(defaultValue = "10") int perPage,
                                                           @RequestParam(required = false) UUID postId,
                                                           @RequestParam(required = false, defaultValue = "PENDING") List<ReportPostStatus> reportPostStatusList,
                                                           @RequestParam(required = false) String username) {
        return reportService.getAllReports(page, perPage, postId, reportPostStatusList, username).thenApply(reportResponses ->
                ApiResponse.<List<ReportResponse>>builder()
                        .entity(reportResponses)
                        .build()
        ).join();
    }

    @Operation(summary = "Get All Post Reports For Staff")
    @GetMapping(path = "/getall/for-staff")
    public ApiResponse<List<ReportResponse>> getAllReportsForStaff(@RequestParam(defaultValue = "1") int page,
                                                                   @RequestParam(defaultValue = "10") int perPage,
                                                                   @RequestParam(required = false, defaultValue = "PENDING") List<ReportPostStatus> reportPostStatusList,
                                                                   @RequestParam(required = false) String username) {
        return reportService.getAllReportsForStaff(page, perPage, reportPostStatusList, username).thenApply(reportResponses ->
                ApiResponse.<List<ReportResponse>>builder()
                        .entity(reportResponses)
                        .build()
        ).join();
    }

    @Operation(summary = "Get Post Report", description = "Get Post Report By ID")
    @GetMapping(path = "/get/{reportId}")
    public ApiResponse<ReportResponse> getReportById(@PathVariable UUID reportId) {
        return reportService.getReportById(reportId).thenApply(reportResponse ->
                ApiResponse.<ReportResponse>builder()
                        .entity(reportResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Update Post Report", description = "Update Post Report By ID")
    @PutMapping(path = "/update/{reportId}")
    public ApiResponse<ReportResponse> updateReportById(@PathVariable UUID reportId,
                                                        @RequestParam ReportPostUpdateStatus status,
                                                        @RequestParam(required = false) UUID clientSessionId) {
        return reportService.updateReportStatus(clientSessionId,reportId, status).thenApply(reportResponse ->
                ApiResponse.<ReportResponse>builder()
                        .message(SuccessReturnMessage.UPDATE_SUCCESS.getMessage())
                        .entity(reportResponse)
                        .build()
        ).join();
    }
}
