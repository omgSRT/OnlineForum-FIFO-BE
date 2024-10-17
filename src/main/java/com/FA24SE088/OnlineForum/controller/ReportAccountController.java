package com.FA24SE088.OnlineForum.controller;

import com.FA24SE088.OnlineForum.dto.request.ReportAccountRequest;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.ReportAccountResponse;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.service.ReportAccountService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/report-account")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReportAccountController {
    final ReportAccountService reportAccountService;

    @PostMapping
    public ApiResponse<ReportAccountResponse> createReportAccount(@RequestBody ReportAccountRequest reportAccountRequest) {
        return ApiResponse.<ReportAccountResponse>builder()
                .entity(reportAccountService.createReportAccount(reportAccountRequest))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<ReportAccountResponse> updateReportAccount(@PathVariable UUID id, @RequestBody ReportAccountRequest reportAccountRequest) {
        return ApiResponse.<ReportAccountResponse>builder()
                .entity(reportAccountService.updateReportAccount(id, reportAccountRequest)
                        .orElseThrow(() -> new AppException(ErrorCode.REPORT_ACCOUNT_NOT_FOUND)))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ReportAccountResponse> getReportAccount(@PathVariable UUID id) {
        return ApiResponse.<ReportAccountResponse>builder()
                .entity(reportAccountService.getReportAccountById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.REPORT_ACCOUNT_NOT_FOUND)))
                .build();
    }

    @GetMapping
    public ApiResponse<List<ReportAccountResponse>> getAllReportAccounts() {
        return ApiResponse.<List<ReportAccountResponse>>builder()
                .entity(reportAccountService.getAllReportAccounts())
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteReportAccount(@PathVariable UUID id) {
        reportAccountService.deleteReportAccount(id);
        return ApiResponse.<Void>builder().build();
    }
}
