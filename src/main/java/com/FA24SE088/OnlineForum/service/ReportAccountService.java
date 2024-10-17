package com.FA24SE088.OnlineForum.service;


import com.FA24SE088.OnlineForum.dto.request.FeedbackRequest;
import com.FA24SE088.OnlineForum.dto.request.FeedbackRequest2;
import com.FA24SE088.OnlineForum.dto.request.ReportAccountRequest;
import com.FA24SE088.OnlineForum.dto.response.FeedbackResponse;
import com.FA24SE088.OnlineForum.dto.response.ReportAccountResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Feedback;
import com.FA24SE088.OnlineForum.entity.ReportAccount;
import com.FA24SE088.OnlineForum.enums.FeedbackStatus;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.FeedbackMapper;
import com.FA24SE088.OnlineForum.mapper.ReportAccountMapper;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@Service
public class ReportAccountService {

    @Autowired
    UnitOfWork unitOfWork;

    @Autowired
    ReportAccountMapper reportAccountMapper;

    private Account getCurrentUser() {
        var context = SecurityContextHolder.getContext();
        return unitOfWork.getAccountRepository().findByUsername(context.getAuthentication().getName())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    public ReportAccountResponse createReportAccount(ReportAccountRequest reportAccountRequest) {
        Account reporter = getCurrentUser();
        Account reported = unitOfWork.getAccountRepository()
                .findById(reportAccountRequest.getReportedId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        ReportAccount reportAccount = reportAccountMapper.toReportAccount(reportAccountRequest);
        reportAccount.setReporter(reporter);
        reportAccount.setReported(reported);
        reportAccount.setReportTime(new Date());
        reportAccount.setStatus("PENDING");

        ReportAccount savedReportAccount = unitOfWork.getReportAccountRepository().save(reportAccount);
        return reportAccountMapper.toResponse(savedReportAccount);
    }

    public Optional<ReportAccountResponse> updateReportAccount(UUID reportAccountId, ReportAccountRequest reportAccountRequest) {
        Optional<ReportAccount> reportAccountOptional = unitOfWork.getReportAccountRepository().findById(reportAccountId);

        if (reportAccountOptional.isPresent()) {
            ReportAccount reportAccount = reportAccountOptional.get();
            reportAccount.setReason(reportAccountRequest.getReason());
            reportAccount.setStatus(reportAccountRequest.getStatus());

            ReportAccount updatedReportAccount = unitOfWork.getReportAccountRepository().save(reportAccount);
            return Optional.of(reportAccountMapper.toResponse(updatedReportAccount));
        }
        return Optional.empty();
    }

    public Optional<ReportAccountResponse> getReportAccountById(UUID reportAccountId) {
        Optional<ReportAccount> reportAccountOptional = unitOfWork.getReportAccountRepository().findById(reportAccountId);
        return reportAccountOptional.map(reportAccountMapper::toResponse);
    }

    public List<ReportAccountResponse> getAllReportAccounts() {
        List<ReportAccount> reportAccounts = unitOfWork.getReportAccountRepository().findAll();
        return reportAccounts.stream()
                .map(reportAccountMapper::toResponse)
                .collect(Collectors.toList());
    }

    public void deleteReportAccount(UUID reportAccountId) {
        if (unitOfWork.getReportAccountRepository().existsById(reportAccountId)) {
            unitOfWork.getReportAccountRepository().deleteById(reportAccountId);
        } else {
            throw new AppException(ErrorCode.REPORT_ACCOUNT_NOT_FOUND);
        }
    }
}


