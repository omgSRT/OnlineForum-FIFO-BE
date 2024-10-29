package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.ReportRequest;
import com.FA24SE088.OnlineForum.dto.response.ReportResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Post;
import com.FA24SE088.OnlineForum.entity.Report;
import com.FA24SE088.OnlineForum.enums.*;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.ReportMapper;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import com.FA24SE088.OnlineForum.utils.PaginationUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class ReportService {
    UnitOfWork unitOfWork;
    ReportMapper reportMapper;
    PaginationUtils paginationUtils;

    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    @Async("AsyncTaskExecutor")
    public CompletableFuture<ReportResponse> createReport(ReportRequest request, ReportPostReason reportPostReason){
        var postFuture = findPostById(request.getPostId());
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);

        return CompletableFuture.allOf(postFuture, accountFuture).thenCompose(v -> {
            var post = postFuture.join();
            var account = accountFuture.join();

            Report newReport = reportMapper.toReport(request);
            newReport.setTitle(reportPostReason.name());
            newReport.setDescription(reportPostReason.getMessage());
            newReport.setPost(post);
            newReport.setAccount(account);
            newReport.setReportTime(new Date());
            newReport.setStatus(ReportPostStatus.PENDING.name());

            return CompletableFuture.completedFuture(unitOfWork.getReportRepository().save(newReport));
        })
                .thenApply(reportMapper::toReportResponse);
    }
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Async("AsyncTaskExecutor")
    public CompletableFuture<ReportResponse> deleteReportById(UUID reportId){
        var reportFuture = findReportById(reportId);

        return reportFuture.thenCompose(report -> {
                    unitOfWork.getReportRepository().delete(report);

                    return CompletableFuture.completedFuture(report);
                })
                .thenApply(reportMapper::toReportResponse);
    }
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Async("AsyncTaskExecutor")
    public CompletableFuture<List<ReportResponse>> filterAllReports(int page, int perPage, UUID postId, List<ReportPostStatus> reportPostStatusList){
        var postFuture = postId != null
                ? findPostById(postId)
                : CompletableFuture.completedFuture(null);

        return postFuture.thenCompose(post -> {
                    var list = unitOfWork.getReportRepository().findAllByOrderByReportTimeDesc().stream()
                            .filter(report -> post == null || report.getPost().equals(post))
                            .filter(report -> reportPostStatusList == null || reportPostStatusList.isEmpty() ||
                                    (safeValueOf(report.getStatus()) != null
                                            && reportPostStatusList.contains(safeValueOf(report.getStatus()))))
                            .map(reportMapper::toReportResponse)
                            .toList();

                    var paginatedList = paginationUtils.convertListToPage(page, perPage, list);

                    return CompletableFuture.completedFuture(paginatedList);
                });
    }
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Async("AsyncTaskExecutor")
    public CompletableFuture<List<ReportResponse>> getAllReports(int page, int perPage){
        return CompletableFuture.supplyAsync(() -> {
            var list = unitOfWork.getReportRepository().findAllByOrderByReportTimeDesc().stream()
                    .map(reportMapper::toReportResponse)
                    .toList();

            return paginationUtils.convertListToPage(page, perPage, list);
        });
    }
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Async("AsyncTaskExecutor")
    public CompletableFuture<List<ReportResponse>> getAllReportsByAccountUsername(int page, int perPage, String username){
        var reportListFuture = findReportsByAccountUsername(username);

        return reportListFuture.thenCompose(reportList -> {
            var reportResponseList = reportList.stream()
                    .map(reportMapper::toReportResponse)
                    .toList();

            var paginatedList = paginationUtils.convertListToPage(page, perPage, reportResponseList);

            return CompletableFuture.completedFuture(paginatedList);
        });
    }
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Async("AsyncTaskExecutor")
    public CompletableFuture<ReportResponse> getReportById(UUID reportId){
        var reportFuture = findReportById(reportId);

        return reportFuture.thenApply(reportMapper::toReportResponse);
    }
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Async("AsyncTaskExecutor")
    public CompletableFuture<ReportResponse> updateReportStatus(UUID reportId, ReportPostUpdateStatus status) {
        var reportFuture = findReportById(reportId);

        return reportFuture.thenCompose(report -> {
                if(!report.getStatus().equals(ReportPostStatus.PENDING.name())){
                    throw new AppException(ErrorCode.REPORT_POST_NOT_PENDING);
                }

                report.setStatus(status.name());

                return CompletableFuture.completedFuture(unitOfWork.getReportRepository().save(report));
        })
                .thenApply(reportMapper::toReportResponse);
    }


    @Async("AsyncTaskExecutor")
    private CompletableFuture<Post> findPostById(UUID postId) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getPostRepository().findById(postId)
                        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND))
        );
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<Report> findReportById(UUID feedbackId) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getReportRepository().findById(feedbackId)
                        .orElseThrow(() -> new AppException(ErrorCode.FEEDBACK_NOT_FOUND))
        );
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<Report>> findReportsByAccountUsername(String username) {
        return unitOfWork.getReportRepository().findByAccountUsernameContainingOrderByReportTimeDesc(username);
    }
    private ReportPostStatus safeValueOf(String status) {
        try {
            return ReportPostStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String getUsernameFromJwt() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaim("username");  // Get the "username" claim from the token
        }
        return null;
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<Account> findAccountByUsername(String username) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getAccountRepository().findByUsername(username)
                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND))
        );
    }
}
