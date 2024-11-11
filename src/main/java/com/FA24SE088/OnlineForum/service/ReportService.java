package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.ReportRequest;
import com.FA24SE088.OnlineForum.dto.response.PostResponse;
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

    @PreAuthorize("hasRole('USER')")
    @Async("AsyncTaskExecutor")
    public CompletableFuture<ReportResponse> createReport(ReportRequest request, ReportPostReason reportPostReason){
        var postFuture = findPostById(request.getPostId());
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);

        return CompletableFuture.allOf(postFuture, accountFuture).thenCompose(v -> {
            var post = postFuture.join();
            var account = accountFuture.join();

            if(reportPostReason == null){
                throw new AppException(ErrorCode.REPORT_POST_REASON_NOT_FOUND);
            }
            if(account.equals(post.getAccount())){
                throw new AppException(ErrorCode.CANNOT_REPORT_SELF_POST);
            }

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
    public CompletableFuture<List<ReportResponse>> getAllReports(int page, int perPage,
                                                                 UUID postId,
                                                                 List<ReportPostStatus> reportPostStatusList,
                                                                 String name){
        var postFuture = postId != null
                ? findPostById(postId)
                : CompletableFuture.completedFuture(null);

        return postFuture.thenCompose(post -> {
                    var list = unitOfWork.getReportRepository().findAllByOrderByReportTimeDesc().stream()
                            .filter(report -> post == null || report.getPost().equals(post))
                            .filter(report -> reportPostStatusList == null || reportPostStatusList.isEmpty() ||
                                    (safeValueOf(report.getStatus()) != null
                                            && reportPostStatusList.contains(safeValueOf(report.getStatus()))))
                            .filter(report -> name == null || name.isEmpty() || report.getAccount().getUsername().contains(name))
                            .map(reportMapper::toReportResponse)
                            .toList();

                    var paginatedList = paginationUtils.convertListToPage(page, perPage, list);

                    return CompletableFuture.completedFuture(paginatedList);
                });
    }
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Async("AsyncTaskExecutor")
    public CompletableFuture<List<ReportResponse>> getAllReportsForStaff(int page, int perPage,
                                                                 List<ReportPostStatus> reportPostStatusList,
                                                                 String accountUsername){
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);

        return CompletableFuture.allOf(accountFuture).thenCompose(v -> {
            var account = accountFuture.join();

            return unitOfWork.getCategoryRepository().findByAccount(account).thenCompose(categoryList -> {
                var list = unitOfWork.getReportRepository().findAllByOrderByReportTimeDesc().stream()
                        .filter(report -> categoryList.contains(report.getPost().getTopic().getCategory()))
                        .filter(report -> reportPostStatusList == null || reportPostStatusList.isEmpty() ||
                                (safeValueOf(report.getStatus()) != null
                                        && reportPostStatusList.contains(safeValueOf(report.getStatus()))))
                        .filter(report -> accountUsername == null || accountUsername.isEmpty() || report.getAccount().getUsername().contains(accountUsername))
                        .map(reportMapper::toReportResponse)
                        .toList();

                var paginatedList = paginationUtils.convertListToPage(page, perPage, list);

                return CompletableFuture.completedFuture(paginatedList);
            });
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
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);

        return CompletableFuture.allOf(reportFuture, accountFuture).thenCompose(v -> {
                var report = reportFuture.join();
                var account = accountFuture.join();

                if(!report.getStatus().equals(ReportPostStatus.PENDING.name())){
                    throw new AppException(ErrorCode.REPORT_POST_NOT_PENDING);
                }

//                if(status.name().equals(ReportPostStatus.APPROVED.name())){
//                    return deleteByChangingPostStatusById(report.getPost().getPostId(), account).thenCompose(post -> {
//                        report.setStatus(status.name());
//
//                        return CompletableFuture.completedFuture(unitOfWork.getReportRepository().save(report));
//                    });
//                }

                report.setStatus(status.name());

                return CompletableFuture.completedFuture(unitOfWork.getReportRepository().save(report));
        })
                .thenApply(reportMapper::toReportResponse);
    }


    @Async("AsyncTaskExecutor")
    public CompletableFuture<Post> deleteByChangingPostStatusById(UUID postId, Account account) {
        var postFuture = findPostById(postId);

        return CompletableFuture.allOf(postFuture).thenCompose(v -> {
                    var post = postFuture.join();
                    var categoryPost = post.getTopic().getCategory();

                    return unitOfWork.getCategoryRepository().findByAccount(account).thenCompose(categoryList -> {
                        if (post.getStatus().equals(PostStatus.DRAFT.name())) {
                            throw new AppException(ErrorCode.DRAFT_POST_CANNOT_CHANGE_STATUS);
                        }
                        if (post.getStatus().equals(PostStatus.HIDDEN.name())){
                            throw new AppException(ErrorCode.POST_ALREADY_HIDDEN);
                        }

                        if (account.getRole().getName().equals("STAFF") &&
                                !categoryList.contains(categoryPost)) {
                            throw new AppException(ErrorCode.STAFF_NOT_SUPERVISE_CATEGORY);
                        }

                        post.setStatus(PostStatus.HIDDEN.name());
                        post.setLastModifiedDate(new Date());

                        return CompletableFuture.completedFuture(unitOfWork.getPostRepository().save(post));
                    });
                });
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<Post> findPostById(UUID postId) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getPostRepository().findById(postId)
                        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND))
        );
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<Report> findReportById(UUID reportId) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getReportRepository().findById(reportId)
                        .orElseThrow(() -> new AppException(ErrorCode.REPORT_POST_NOT_FOUND))
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
