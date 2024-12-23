package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.PointRequest;
import com.FA24SE088.OnlineForum.dto.response.PointResponse;
import com.FA24SE088.OnlineForum.entity.*;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.PointMapper;
import com.FA24SE088.OnlineForum.repository.AccountRepository;
import com.FA24SE088.OnlineForum.repository.PointRepository;
import com.FA24SE088.OnlineForum.repository.PostRepository;
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

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class PointService {
    PointRepository pointRepository;
    AccountRepository accountRepository;
    PostRepository postRepository;
    PointMapper pointMapper;
    PaginationUtils paginationUtils;

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<PointResponse> createPoint(PointRequest request) {
        var countPointDataExistFuture = CompletableFuture.supplyAsync(pointRepository::count);

        return countPointDataExistFuture.thenApply(count -> {
            if (count > 0) {
                throw new AppException(ErrorCode.POINT_DATA_EXIST);
            }

            request.setMaxPoint(request.getMaxPoint() <= 0 ? 100 : request.getMaxPoint());
            request.setPointPerPost(request.getPointPerPost() <= 0 ? 5 : request.getPointPerPost());
            request.setPointCostPerDownload(request.getPointCostPerDownload() <= 0 ? 15 : request.getPointCostPerDownload());
            request.setPointEarnedPerDownload(request.getPointEarnedPerDownload() <= 0 ? 2 : request.getPointEarnedPerDownload());
            request.setReportThresholdForAutoDelete(request.getReportThresholdForAutoDelete() <= 0 ? 5 : request.getReportThresholdForAutoDelete());
            if (request.getMaxPoint() < request.getPointPerPost()) {
                throw new AppException(ErrorCode.MAX_POINT_LOWER_THAN_INDIVIDUAL_POINT);
            }

            var newPoint = pointMapper.toPoint(request);

            return pointMapper.toPointResponse(pointRepository.save(newPoint));
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<List<PointResponse>> getAllPoints() {
        return CompletableFuture.supplyAsync(() ->
                pointRepository.findAll().stream()
                        .map(pointMapper::toPointResponse)
                        .toList());
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<PointResponse> updatePoint(PointRequest request) {
        var listPointDataFuture = CompletableFuture.supplyAsync(pointRepository::findAll);

        return listPointDataFuture.thenApply(listPointData -> {
            Point existPoint;

            if (!listPointData.isEmpty()) {
                existPoint = listPointData.get(0);
            } else {
                throw new AppException(ErrorCode.POINT_NOT_FOUND);
            }

            request.setMaxPoint(request.getMaxPoint() <= 0
                    ? existPoint.getMaxPoint()
                    : request.getMaxPoint());
            request.setPointPerPost(request.getPointPerPost() <= 0
                    ? existPoint.getPointPerPost()
                    : request.getPointPerPost());
            request.setPointCostPerDownload(request.getPointCostPerDownload() <= 0
                    ? existPoint.getPointCostPerDownload()
                    : request.getPointCostPerDownload());
            request.setPointEarnedPerDownload(request.getPointEarnedPerDownload() <= 0
                    ? existPoint.getPointEarnedPerDownload()
                    : request.getPointEarnedPerDownload());
            request.setReportThresholdForAutoDelete(request.getReportThresholdForAutoDelete() <= 0
                    ? existPoint.getReportThresholdForAutoDelete()
                    : request.getReportThresholdForAutoDelete());
            if (request.getMaxPoint() < request.getPointPerPost()) {
                throw new AppException(ErrorCode.MAX_POINT_LOWER_THAN_INDIVIDUAL_POINT);
            }

            pointMapper.updatePoint(existPoint, request);

            return pointMapper.toPointResponse(pointRepository.save(existPoint));
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<PointResponse> deletePoint() {
        var listPointDataFuture = CompletableFuture.supplyAsync(pointRepository::findAll);

        return listPointDataFuture.thenApply(listPointData -> {
            Point existPoint = new Point();

            if (!listPointData.isEmpty()) {
                existPoint = listPointData.get(0);
            } else {
                throw new AppException(ErrorCode.POINT_NOT_FOUND);
            }

            pointRepository.delete(existPoint);

            return pointMapper.toPointResponse(existPoint);
        });
    }

    private String getUsernameFromJwt() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaim("username");
        }
        return null;
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<Account> findAccountByUsername(String username) {
        return CompletableFuture.supplyAsync(() ->
                accountRepository.findByUsername(username)
                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND))
        );
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<Post> findPostById(UUID postId) {
        return CompletableFuture.supplyAsync(() ->
                postRepository.findById(postId)
                        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND))
        );
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<Account> findAccountById(UUID accountId) {
        return CompletableFuture.supplyAsync(() ->
                accountRepository.findById(accountId)
                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND))
        );
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<Point>> getPoint() {
        return CompletableFuture.supplyAsync(() ->
                pointRepository.findAll().stream()
                        .toList());
    }
}
