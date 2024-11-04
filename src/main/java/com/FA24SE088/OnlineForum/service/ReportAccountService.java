package com.FA24SE088.OnlineForum.service;


import com.FA24SE088.OnlineForum.dto.response.ReportAccount2Response;
import com.FA24SE088.OnlineForum.dto.response.ReportAccountResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Post;
import com.FA24SE088.OnlineForum.entity.ReportAccount;
import com.FA24SE088.OnlineForum.enums.ReportAccountReason;
import com.FA24SE088.OnlineForum.enums.ReportAccountStatus;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.ReportAccountMapper;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class ReportAccountService {
    UnitOfWork unitOfWork;
    ReportAccountMapper reportAccountMapper;

    private Account getCurrentUser() {
        var context = SecurityContextHolder.getContext();
        return unitOfWork.getAccountRepository().findByUsername(context.getAuthentication().getName()).orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }


    public CompletableFuture<ReportAccount2Response> createReportAccount1(UUID reportedId, ReportAccountReason reason) {
        Account reporter = getCurrentUser();
        var reported = CompletableFuture.supplyAsync(() ->
                unitOfWork.getAccountRepository()
                        .findById(reportedId)
                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND))
        );

        return CompletableFuture.allOf(reported)
                .thenCompose(v -> {
                    var reported1 = reported.join();

                    if (reporter.getAccountId().equals(reported1.getAccountId())) {
                        throw new AppException(ErrorCode.CANNOT_REPORT_YOURSELF);
                    }

                    boolean check = unitOfWork.getReportAccountRepository().existsByReporterAndReported(reporter, reported1);
                    if (check) throw new AppException(ErrorCode.YOU_HAVE_REPORTED_THIS_ACCOUNT);

                    // Tìm tất cả bài post của người bị report
                    return unitOfWork.getPostRepository().findByAccount(reported1)
                            .thenCompose(postList -> {
                                ReportAccount reportAccount = ReportAccount.builder()
                                        .title(reason.name())
                                        .reason(reason.getMessage())
                                        .reporter(reporter)
                                        .reported(reported1)
                                        .status("PENDING")
                                        .reportTime(new Date())
                                        .build();

                                return CompletableFuture.completedFuture(unitOfWork.getReportAccountRepository().save(reportAccount))
                                        .thenApply(savedReportAccount -> {
                                            ReportAccount2Response response = reportAccountMapper.toResponse2(savedReportAccount);
                                            response.setPostOfReportedList(postList);
                                            return response;
                                        });
                            });
                });
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

//    @Async("AsyncTaskExecutor")
//    public CompletableFuture<ReportAccount2Response> createReportAccount(UUID reportedId, ReportAccountReason reason) {
//        var reporter = getCurrentUser();
//        var reported = CompletableFuture.supplyAsync(() -> {
//            return unitOfWork.getAccountRepository()
//                    .findById(reportedId)
//                    .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
//        });
//
//        return reporter.thenCombine(reported, (reporter1, reported1) -> {
//            if (reporter1.getAccountId().equals(reported1.getAccountId())) {
//                throw new AppException(ErrorCode.CANNOT_REPORT_YOURSELF);
//            }
//
//            boolean check = unitOfWork.getReportAccountRepository().existsByReporterAndReported(reporter1, reported1);
//            if (check) throw new AppException(ErrorCode.YOU_HAVE_REPORTED_THIS_ACCOUNT);
//
//            return unitOfWork.getPostRepository().findByAccount(reported1)
//                    .thenApply(postList -> {
//                        ReportAccount reportAccount = ReportAccount.builder()
//                                .title(reason.name())
//                                .reason(reason.getMessage())
//                                .reporter(reporter1)
//                                .reported(reported1)
//                                .status("PENDING")
//                                .reportTime(new Date())
//                                .build();
//
//                        ReportAccount savedReportAccount = unitOfWork.getReportAccountRepository().save(reportAccount);
//                        ReportAccount2Response response = reportAccountMapper.toResponse2(savedReportAccount);
//                        response.setPostOfReportedList(postList);
//
//                        return response;
//                    });
//        }).thenCompose(Function.identity());
//    }


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
        List<ReportAccountResponse> reportAccountList = new ArrayList<>(unitOfWork.getReportAccountRepository().findAll().stream()
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


