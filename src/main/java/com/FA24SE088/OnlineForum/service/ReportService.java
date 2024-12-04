package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.ReportRequest;
import com.FA24SE088.OnlineForum.dto.response.DataNotification;
import com.FA24SE088.OnlineForum.dto.response.PostResponse;
import com.FA24SE088.OnlineForum.dto.response.ReportResponse;
import com.FA24SE088.OnlineForum.entity.*;
import com.FA24SE088.OnlineForum.enums.*;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.ReportMapper;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import com.FA24SE088.OnlineForum.utils.PaginationUtils;
import com.FA24SE088.OnlineForum.utils.SocketIOUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class ReportService {
    UnitOfWork unitOfWork;
    ReportMapper reportMapper;
    PaginationUtils paginationUtils;
    ObjectMapper objectMapper = new ObjectMapper();
    SocketIOUtil socketIOUtil;

    @PreAuthorize("hasRole('USER')")
    @Async("AsyncTaskExecutor")
    public CompletableFuture<ReportResponse> createReport(ReportRequest request, ReportPostReason reportPostReason) {
        var postFuture = findPostById(request.getPostId());
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);

        return CompletableFuture.allOf(postFuture, accountFuture).thenCompose(v -> {
                    var post = postFuture.join();
                    var account = accountFuture.join();
                    var postOwner = post.getAccount();
                    var postOwnerRole = postOwner.getRole().getName();

                    if (postOwnerRole.equalsIgnoreCase("ADMIN")) {
                        throw new AppException(ErrorCode.CANNOT_REPORT_ADMIN_POST);
                    }

                    if (reportPostReason == null) {
                        throw new AppException(ErrorCode.REPORT_POST_REASON_NOT_FOUND);
                    }
                    if (account.equals(post.getAccount())) {
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
    public CompletableFuture<ReportResponse> deleteReportById(UUID reportId) {
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
                                                                 String name) {
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
                                                                         String accountUsername) {
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);

        return CompletableFuture.allOf(accountFuture).thenCompose(v -> {
            var account = accountFuture.join();

            if (account.getRole().getName().equalsIgnoreCase("ADMIN")) {
                return unitOfWork.getCategoryRepository().findByAccount(account).thenCompose(categoryList -> {
                    var list = unitOfWork.getReportRepository().findAllByOrderByReportTimeDesc().stream()
                            .filter(report -> reportPostStatusList == null || reportPostStatusList.isEmpty() ||
                                    (safeValueOf(report.getStatus()) != null
                                            && reportPostStatusList.contains(safeValueOf(report.getStatus()))))
                            .filter(report -> accountUsername == null || accountUsername.isEmpty()
                                    || report.getAccount().getUsername().contains(accountUsername))
                            .map(reportMapper::toReportResponse)
                            .toList();

                    var paginatedList = paginationUtils.convertListToPage(page, perPage, list);

                    return CompletableFuture.completedFuture(paginatedList);
                });
            }

            return unitOfWork.getCategoryRepository().findByAccount(account).thenCompose(categoryList -> {
                var list = unitOfWork.getReportRepository().findAllByOrderByReportTimeDesc().stream()
                        .filter(report -> categoryList.contains(report.getPost().getTopic().getCategory()))
                        .filter(report -> reportPostStatusList == null || reportPostStatusList.isEmpty() ||
                                (safeValueOf(report.getStatus()) != null
                                        && reportPostStatusList.contains(safeValueOf(report.getStatus()))))
                        .filter(report -> accountUsername == null || accountUsername.isEmpty()
                                || report.getAccount().getUsername().contains(accountUsername))
                        .map(reportMapper::toReportResponse)
                        .toList();

                var paginatedList = paginationUtils.convertListToPage(page, perPage, list);

                return CompletableFuture.completedFuture(paginatedList);
            });
        });
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Async("AsyncTaskExecutor")
    public CompletableFuture<ReportResponse> getReportById(UUID reportId) {
        var reportFuture = findReportById(reportId);

        return reportFuture.thenApply(reportMapper::toReportResponse);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Async("AsyncTaskExecutor")
    public CompletableFuture<ReportResponse> updateReportStatus(UUID reportId, ReportPostUpdateStatus status) {
        long maxReportPost = 1;
        var reportFuture = findReportById(reportId);
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);
        var pointFuture = getPoint();

        return CompletableFuture.allOf(reportFuture, accountFuture, pointFuture).thenCompose(v -> {
                    var report = reportFuture.join();


                    if (!report.getStatus().equals(ReportPostStatus.PENDING.name())) {
                        throw new AppException(ErrorCode.REPORT_POST_NOT_PENDING);
                    }

//                    if (status.name().equals(ReportPostStatus.APPROVED.name())) {
//                        return walletPostOwnerFuture.thenCompose(walletPostOwner -> {
//                            if (walletPostOwner != null) {
//                                createTransaction(walletPostOwner, point);
//                                var pointDeduction = walletPostOwner.getBalance() - point.getPointPerPost();
//                                walletPostOwner.setBalance(pointDeduction);
//                                unitOfWork.getWalletRepository().save(walletPostOwner);
//
//                                report.setStatus(status.name());
//                                return CompletableFuture.completedFuture(unitOfWork.getReportRepository().save(report));
//                            } else {
//                                return CompletableFuture.completedFuture(null);
//                            }
//                        });
//                    }

                    report.setStatus(status.name());

                    return CompletableFuture.completedFuture(unitOfWork.getReportRepository().save(report));
                })
                .thenCompose(report -> unitOfWork.getReportRepository()
                        .countByPostAndStatus(report.getPost(), ReportPostStatus.APPROVED.name())
                        .thenCompose(count -> {
                            var pointList = pointFuture.join();
                            var account = accountFuture.join();
                            if (pointList.isEmpty()) {
                                throw new AppException(ErrorCode.POINT_NOT_FOUND);
                            }
                            Point point = pointList.get(0);
                            var postOwner = report.getPost().getAccount();
                            var walletPostOwnerFuture = unitOfWork.getWalletRepository().findByAccount(postOwner);

                            if (count >= maxReportPost) {
                                walletPostOwnerFuture.thenCompose(walletPostOwner -> {
                                    if (walletPostOwner != null) {
                                        createTransaction(walletPostOwner, point);
                                        var pointDeduction = walletPostOwner.getBalance() - point.getPointPerPost();
                                        walletPostOwner.setBalance(pointDeduction);
                                        var post = report.getPost();
                                        post.setStatus(PostStatus.HIDDEN.name());
                                        realtimeNotificationForReported(report, "Report", "Your post has been violated and deleted");
                                        realtimeNotificationForReporter(report, "Report", "The staff has processing the post you have reported");
                                        realtimeNotificationForStaff(report, account, "This is the 5th Approve and the post was deleted", "Report");
                                        unitOfWork.getWalletRepository().save(walletPostOwner);
                                        unitOfWork.getPostRepository().save(post);
                                        return CompletableFuture.completedFuture(null);
                                    }
                                    return null;
                                });
                            }
                            return CompletableFuture.completedFuture(report);
                        }))
                .thenApply(reportMapper::toReportResponse);
    }

    public void realtimeNotificationForReported(Report report, String entity, String title) {
        DataNotification dataNotification = DataNotification.builder()
                .id(report.getReportId())
                .entity(entity)
                .build();
        String messageJson = null;
        try {
            messageJson = objectMapper.writeValueAsString(dataNotification);
            Notification notification = Notification.builder()
                    .title(title)
                    .message(messageJson)
                    .isRead(false)
                    .account(report.getPost().getAccount())
                    .createdDate(LocalDateTime.now())
                    .build();
            unitOfWork.getNotificationRepository().save(notification);
            socketIOUtil.sendEventToOneClientInAServer(report.getPost().getAccount().getAccountId(), WebsocketEventName.NOTIFICATION.name(), notification);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void realtimeNotificationForReporter(Report report, String title, String entity) {
        DataNotification dataNotification = DataNotification.builder()
                .id(report.getReportId())
                .entity(entity)
                .build();
        String messageJson = null;
        try {
            messageJson = objectMapper.writeValueAsString(dataNotification);
            Notification notification = Notification.builder()
                    .title(title)
                    .message(messageJson)
                    .isRead(false)
                    .account(report.getAccount())
                    .createdDate(LocalDateTime.now())
                    .build();
            unitOfWork.getNotificationRepository().save(notification);
            socketIOUtil.sendEventToOneClientInAServer(report.getAccount().getAccountId(), WebsocketEventName.NOTIFICATION.name(), notification);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void realtimeNotificationForStaff(Report report, Account account, String title, String entity) {
        DataNotification dataNotification = DataNotification.builder()
                .id(report.getReportId())
                .entity(entity)
                .build();
        String messageJson = null;
        try {
            messageJson = objectMapper.writeValueAsString(dataNotification);
            Notification notification = Notification.builder()
                    .title(title)
                    .message(messageJson)
                    .isRead(false)
                    .account(account)
                    .createdDate(LocalDateTime.now())
                    .build();
            unitOfWork.getNotificationRepository().save(notification);
            socketIOUtil.sendEventToOneClientInAServer(account.getAccountId(), WebsocketEventName.NOTIFICATION.name(), notification);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
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
                if (post.getStatus().equals(PostStatus.HIDDEN.name())) {
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

    @Async("AsyncTaskExecutor")
    private CompletableFuture<Void> createTransaction(Wallet wallet, Point point) {
        return CompletableFuture.supplyAsync(() -> {
            var pointDeduction = point.getPointPerPost() * -1;

            Transaction newTransaction = Transaction.builder()
                    .amount(pointDeduction)
                    .createdDate(new Date())
                    .wallet(wallet)
                    .reward(null)
                    .transactionType(TransactionType.POST_VIOLATION.name())
                    .build();

            unitOfWork.getTransactionRepository().save(newTransaction);

            return null;
        });
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<Point>> getPoint() {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getPointRepository().findAll().stream()
                        .toList());
    }
}
