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
import com.FA24SE088.OnlineForum.enums.ReportAccountReason;
import com.FA24SE088.OnlineForum.enums.ReportAccountStatus;
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

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class ReportAccountService {
    UnitOfWork unitOfWork;
    ReportAccountMapper reportAccountMapper;

    private Account getCurrentUser() {
        var context = SecurityContextHolder.getContext();
        return unitOfWork.getAccountRepository().findByUsername(context.getAuthentication().getName())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }


    public ReportAccountResponse createReportAccount(UUID reportedId, ReportAccountReason reason) {
        Account reporter = getCurrentUser();
        Account reported = unitOfWork.getAccountRepository()
                .findById(reportedId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        if (reporter.getAccountId().equals(reported.getAccountId())) {
            throw new AppException(ErrorCode.CANNOT_REPORT_YOURSELF);
        }
        boolean check = unitOfWork.getReportAccountRepository().existsByReporterAndReported(reporter, reported);
        if (check) throw new AppException(ErrorCode.YOU_HAVE_REPORTED_THIS_ACCOUNT);
        ReportAccount reportAccount = ReportAccount.builder()
                .title(reason.name())
                .reason(reason.getMessage())
                .reporter(reporter)
                .reported(reported)
                .status("PENDING")
                .reportTime(new Date())
                .build();


        ReportAccount savedReportAccount = unitOfWork.getReportAccountRepository().save(reportAccount);
        savedReportAccount.setReporter(reporter);
        savedReportAccount.setReported(reported);
        ReportAccountResponse response = reportAccountMapper.toResponse(savedReportAccount);
        return response;
    }

    public Optional<ReportAccountResponse> updateReportAccount(UUID reportAccountId, ReportAccountStatus status) {
        Optional<ReportAccount> reportAccountOptional = unitOfWork.getReportAccountRepository().findById(reportAccountId);

        if (reportAccountOptional.isPresent()) {
            ReportAccount reportAccount = reportAccountOptional.get();
            reportAccount.setStatus(status.name());
            ReportAccount updatedReportAccount = unitOfWork.getReportAccountRepository().save(reportAccount);
            return Optional.of(reportAccountMapper.toResponse(updatedReportAccount));
        }

        return Optional.empty();
    }

    public Optional<ReportAccountResponse> getReportAccountById(UUID reportAccountId) {
        Optional<ReportAccount> reportAccountOptional = unitOfWork.getReportAccountRepository().findById(reportAccountId);
        return reportAccountOptional.map(reportAccountMapper::toResponse);
    }

    public List<ReportAccountResponse> filter(String reporter, String reported, ReportAccountStatus status, boolean ascending) {
        List<ReportAccountResponse> reportAccountList = new ArrayList<>( unitOfWork.getReportAccountRepository().findAll().stream()
                .map(reportAccountMapper::toResponse)
                .filter(reportAccountResponse -> (reporter == null || (reportAccountResponse.getReporter().getUsername() != null && reportAccountResponse.getReporter().getUsername().contains(reporter))))
                .filter(reportAccountResponse -> (reported == null) || (reportAccountResponse.getReported().getUsername() != null && reportAccountResponse.getReported().getUsername().contains(reported)))
                .filter(reportAccountResponse -> (status == null || (reportAccountResponse.getStatus() != null && reportAccountResponse.getStatus().contains(status.name()))))
                .toList());
        reportAccountList.sort((f1, f2) -> {
            if (ascending) {
                return f1.getReportTime().compareTo(f2.getReportTime());
            } else {
                return f2.getReportTime().compareTo(f1.getReportTime());
            }
        });
        return reportAccountList;
    }

    public List<ReportAccountResponse> getAllReportAccounts() {
        List<ReportAccount> reportAccounts = unitOfWork.getReportAccountRepository().findAll();
        return reportAccounts.stream()
                .map(reportAccountMapper::toResponse)
                .toList();
    }

    public void deleteReportAccount(UUID reportAccountId) {
        if (unitOfWork.getReportAccountRepository().existsById(reportAccountId)) {
            unitOfWork.getReportAccountRepository().deleteById(reportAccountId);
        } else {
            throw new AppException(ErrorCode.REPORT_ACCOUNT_NOT_FOUND);
        }
    }
}


