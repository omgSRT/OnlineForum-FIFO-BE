package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.ReportRequest;
import com.FA24SE088.OnlineForum.dto.response.ReportResponse;
import com.FA24SE088.OnlineForum.entity.Post;
import com.FA24SE088.OnlineForum.entity.Report;
import com.FA24SE088.OnlineForum.enums.ReportReason;
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
import org.springframework.stereotype.Service;

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
    public CompletableFuture<ReportResponse> createReport(ReportRequest request, ReportReason reportReason){
        var postFuture = findPostById(request.getPostId());

        return postFuture.thenCompose(post -> {
            Report newReport = reportMapper.toReport(request);
            newReport.setTitle(reportReason.getTitle());
            newReport.setDescription(reportReason.getDescription());
            newReport.setPost(post);

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
    public CompletableFuture<List<ReportResponse>> getAllReports(int page, int perPage, UUID postId){
        var postFuture = postId != null
                ? findPostById(postId)
                : CompletableFuture.completedFuture(null);

        return postFuture.thenCompose(post -> {
                    var list = unitOfWork.getReportRepository().findAll().stream()
                            .filter(feedback -> post == null || feedback.getPost().equals(post))
                            .map(reportMapper::toReportResponse)
                            .toList();

                    var paginatedList = paginationUtils.convertListToPage(page, perPage, list);

                    return CompletableFuture.completedFuture(paginatedList);
                });
    }
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Async("AsyncTaskExecutor")
    public CompletableFuture<ReportResponse> getReportById(UUID feedbackId){
        var reportFuture = findReportById(feedbackId);

        return reportFuture.thenApply(reportMapper::toReportResponse);
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
}
