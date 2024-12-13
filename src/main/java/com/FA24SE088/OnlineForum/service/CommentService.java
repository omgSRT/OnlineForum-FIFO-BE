package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.CommentCreateRequest;
import com.FA24SE088.OnlineForum.dto.request.CommentGetAllResponse;
import com.FA24SE088.OnlineForum.dto.request.CommentUpdateRequest;
import com.FA24SE088.OnlineForum.dto.request.ReplyCreateRequest;
import com.FA24SE088.OnlineForum.dto.response.*;
import com.FA24SE088.OnlineForum.entity.*;
import com.FA24SE088.OnlineForum.enums.PostStatus;
import com.FA24SE088.OnlineForum.enums.TypeBonusNameEnum;
import com.FA24SE088.OnlineForum.enums.WebsocketEventName;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.CommentMapper;
import com.FA24SE088.OnlineForum.repository.*;
import com.FA24SE088.OnlineForum.utils.ContentFilterUtil;
import com.FA24SE088.OnlineForum.utils.PaginationUtils;
import com.FA24SE088.OnlineForum.utils.SocketIOUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class CommentService {
    CommentRepository commentRepository;
    TypeBonusRepository typeBonusRepository;
    NotificationRepository notificationRepository;
    BlockedAccountRepository blockedAccountRepository;
    PostRepository postRepository;
    AccountRepository accountRepository;
    FollowRepository followRepository;
    DailyPointRepository dailyPointRepository;
    WalletRepository walletRepository;
    CommentMapper commentMapper;
    PaginationUtils paginationUtils;
    SocketIOUtil socketIOUtil;
    ContentFilterUtil contentFilterUtil;
    ObjectMapper objectMapper = new ObjectMapper();

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<CommentResponse> createComment(CommentCreateRequest request) {
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);
        var postFuture = findPostById(request.getPostId());

        return CompletableFuture.allOf(accountFuture, postFuture).thenCompose(v -> {
                    var account = accountFuture.join();
                    var post = postFuture.join();

                    if (post.getStatus().equals(PostStatus.DRAFT.name()) || post.getStatus().equals(PostStatus.HIDDEN.name())) {
                        throw new AppException(ErrorCode.CANNOT_COMMENT_ON_DRAFT);
                    }

                    return commentRepository.countByPost(post).thenCompose(commentCount -> {
                        long amount = commentCount + 1;

                        return typeBonusRepository.findByNameAndQuantity(TypeBonusNameEnum.COMMENT.name(), amount)
                                .thenCompose(typeBonus -> {
                                    if (typeBonus != null) {
                                        return createDailyPointLog(post.getAccount(), post, typeBonus)
                                                .thenCompose(dailyPoint -> {
                                                    var check = checkCommentContentSafe(request.getContent());
                                                    if (!check) {
                                                        throw new AppException(ErrorCode.INAPPROPRIATE_COMMENT);
                                                    }

                                                    Comment newComment = commentMapper.toComment(request);
                                                    newComment.setAccount(account);
                                                    newComment.setPost(post);
                                                    newComment.setParentComment(null);
                                                    newComment.setReplies(new ArrayList<>());

                                                    var savedComment = commentRepository.save(newComment);
                                                    realtimeComment(newComment, "Post", "Comment notification",account.getUsername() + " commented in your post: " + post.getTitle());
                                                    realtimeDailyPointNotification(dailyPoint, "DailyPoint", "Daily point notification","You have been added " + dailyPoint.getPointEarned()+ " points");
                                                    return CompletableFuture.completedFuture(savedComment);
                                                });
                                    } else {
                                        var check = checkCommentContentSafe(request.getContent());
                                        if (!check) {
                                            throw new AppException(ErrorCode.INAPPROPRIATE_COMMENT);
                                        }

                                        Comment newComment = commentMapper.toComment(request);
                                        newComment.setAccount(account);
                                        newComment.setPost(post);
                                        newComment.setParentComment(null);
                                        newComment.setReplies(new ArrayList<>());

                                        var savedComment = commentRepository.save(newComment);
                                        realtimeComment(newComment, "Post", "Comment notification",account.getUsername() + " commented in your post: " + post.getTitle());
                                        return CompletableFuture.completedFuture(savedComment);
                                    }
                                });
                    });
                })
                .thenApply(commentMapper::toCommentResponse);
    }

    public void realtimeComment(Comment comment, String entity, String titleNotification,String message) {
        DataNotification dataNotification = DataNotification.builder()
                .id(comment.getPost().getPostId())
                .entity(entity)
                .build();
        String messageJson = null;
        try {
            messageJson = objectMapper.writeValueAsString(dataNotification);
            Notification notification = Notification.builder()
                    .title(titleNotification)
                    .message(messageJson)
                    .isRead(false)
                    .account(comment.getPost().getAccount())
                    .createdDate(LocalDateTime.now())
                    .build();
            if (!comment.getAccount().getAccountId().equals(comment.getPost().getAccount().getAccountId())) {
                notificationRepository.save(notification);
                socketIOUtil.sendEventToOneClientInAServer(comment.getPost().getAccount().getAccountId(), WebsocketEventName.NOTIFICATION.name(), message,notification);
            }
            socketIOUtil.sendEventToAllClientInAServer(WebsocketEventName.COMMENT.toString(),comment);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void realtimeDailyPointNotification(DailyPoint dailyPoint, String entity, String titleNotification,String message) {
        DataNotification dataNotification = null;
        dataNotification = DataNotification.builder()
                .id(dailyPoint.getDailyPointId())
                .entity(entity)
                .build();
        String messageJson = null;
        try {
            messageJson = objectMapper.writeValueAsString(dataNotification);
            Notification notification = Notification.builder()
                    .title(titleNotification)
                    .message(messageJson)
                    .isRead(false)
                    .account(dailyPoint.getAccount())
                    .createdDate(LocalDateTime.now())
                    .build();
            notificationRepository.save(notification);
            socketIOUtil.sendEventToOneClientInAServer(dailyPoint.getAccount().getAccountId(), WebsocketEventName.NOTIFICATION.name(), message,notification);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<ReplyCreateResponse> createReply(ReplyCreateRequest request) {
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);
        var parentCommentFuture = findCommentById(request.getParentCommentId());

        return CompletableFuture.allOf(accountFuture, parentCommentFuture).thenCompose(v -> {
                    var account = accountFuture.join();
                    var parentComment = parentCommentFuture.join();
                    var post = parentComment.getPost();

                    if (post.getStatus().equals(PostStatus.DRAFT.name()) || post.getStatus().equals(PostStatus.HIDDEN.name())) {
                        throw new AppException(ErrorCode.CANNOT_COMMENT_ON_DRAFT);
                    }

                    return commentRepository.countByPost(post).thenCompose(commentCount -> {
                        long amount = commentCount + 1;

                        return typeBonusRepository.findByNameAndQuantity(TypeBonusNameEnum.COMMENT.name(), amount)
                                .thenCompose(typeBonus -> {
                                    if (typeBonus != null) {
                                        return createDailyPointLog(post.getAccount(), post, typeBonus)
                                                .thenCompose(dailyPoint -> {
                                                    var check = checkCommentContentSafe(request.getContent());
                                                    if (!check) {
                                                        throw new AppException(ErrorCode.INAPPROPRIATE_COMMENT);
                                                    }

                                                    Comment newReply = commentMapper.toCommentFromReplyRequest(request);
                                                    newReply.setAccount(account);
                                                    newReply.setPost(post);
                                                    newReply.setParentComment(parentComment);
                                                    newReply.setReplies(new ArrayList<>());
                                                    var saveNewReply = commentRepository.save(newReply);
                                                    realtimeDailyPointNotification(dailyPoint, "DailyPoint", "Daily point notification","You have been added " + dailyPoint.getPointEarned()+ " points");
                                                    realtimeComment(newReply, "Post", "Reply notification in post: " + post.getTitle(),account.getUsername() + " reply comment"+ parentComment.getContent()+" in your post: " + post.getTitle());
                                                    return CompletableFuture.completedFuture(saveNewReply);
                                                });
                                    } else {
                                        var check = checkCommentContentSafe(request.getContent());
                                        if (!check) {
                                            throw new AppException(ErrorCode.INAPPROPRIATE_COMMENT);
                                        }

                                        Comment newReply = commentMapper.toCommentFromReplyRequest(request);
                                        newReply.setAccount(account);
                                        newReply.setPost(post);
                                        newReply.setParentComment(parentComment);
                                        newReply.setReplies(new ArrayList<>());
                                        var saveNewReply = commentRepository.save(newReply);
                                        realtimeComment(newReply, "Post", "Reply notification in post: " + post.getTitle(),account.getUsername() + " reply comment"+ parentComment.getContent()+" in your post: " + post.getTitle());
                                        return CompletableFuture.completedFuture(saveNewReply);
                                    }
                                });
                    });
                })
                .thenApply(commentMapper::toReplyCreateResponse);
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<List<CommentGetAllResponse>> getAllComments(int page, int perPage) {
        return CompletableFuture.supplyAsync(() -> {
            var list = commentRepository.findAll().stream()
                    .map(commentMapper::toCommentGetAllResponse)
                    .toList();
            return paginationUtils.convertListToPage(page, perPage, list);
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<List<CommentResponse>> getAllCommentsFromOtherUser(int page, int perPage, UUID otherAccountId) {
        var commentListFuture = getAllComments();
        var username = getUsernameFromJwt();
        var currentAccountFuture = findAccountByUsername(username);
        var otherAccountFuture = findAccountById(otherAccountId);
        var blockedListFuture = getBlockedAccountListByUsername(username);

        return CompletableFuture.allOf(commentListFuture, currentAccountFuture, otherAccountFuture, blockedListFuture)
                .thenCompose(v -> {
                    var commentList = commentListFuture.join();
                    var currentAccount = currentAccountFuture.join();
                    var otherAccount = otherAccountFuture.join();
                    var blockedAccountList = blockedListFuture.join();

                    boolean isBlockedByCurrent = blockedAccountList.contains(otherAccount);
                    boolean isBlockedByOther = blockedAccountRepository
                            .findByBlockerAndBlocked(otherAccount, currentAccount).isPresent();
                    boolean isFollowing = isFollowing(currentAccount, otherAccount);
                    boolean isAuthor = currentAccount.equals(otherAccount);
                    boolean isStaffOrAdmin = hasRole(currentAccount, "ADMIN") || hasRole(currentAccount, "STAFF");

                    var responses = commentList.stream()
                            .filter(comment -> comment.getAccount().equals(otherAccount))
                            .filter(comment -> isAuthor || isStaffOrAdmin
                                    || comment.getPost().getStatus().equals(PostStatus.PUBLIC.name())
                                    || (comment.getPost().getStatus().equals(PostStatus.PRIVATE.name()) && isFollowing))
                            .filter(comment -> !isBlockedByCurrent && !isBlockedByOther)
                            .map(commentMapper::toCommentResponse)
                            .toList();

                    var paginatedList = paginationUtils.convertListToPage(page, perPage, responses);

                    return CompletableFuture.completedFuture(paginatedList);
                });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<List<CommentNoPostResponse>> getAllCommentsByPost(int page, int perPage, UUID postId) {
        var postFuture = findPostById(postId);
        var username = getUsernameFromJwt();
        var blockedListFuture = getBlockedAccountListByUsername(username);
        var blockerListFuture = getBlockerAccountListByUsername(username);

        return CompletableFuture.allOf(postFuture, blockerListFuture, blockedListFuture).thenApply(v -> {
            var post = postFuture.join();
            var blockedList = blockedListFuture.join();
            var blockerList = blockerListFuture.join();

            var commentList = commentRepository.findAllByPostWithReplies(post);

            var list = commentList.stream()
                    .filter(comment -> !blockedList.contains(comment.getAccount())
                            || !blockerList.contains(comment.getAccount()))
                    .map(commentMapper::toCommentNoPostResponseWithReplies)
                    .toList();
            return paginationUtils.convertListToPage(page, perPage, list);
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<List<CommentResponse>> getAllCommentsByAccount(int page, int perPage, UUID accountId) {
        var accountFuture = findAccountById(accountId);

        return accountFuture.thenCompose(account ->
                commentRepository.findByAccount(account).thenCompose(list -> {
                    var responseList = list.stream()
                            .map(commentMapper::toCommentResponse)
                            .toList();
                    var paginatedList = paginationUtils.convertListToPage(page, perPage, responseList);

                    return CompletableFuture.completedFuture(paginatedList);
                })
        );
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<CommentResponse> updateComment(UUID commentId, CommentUpdateRequest request) {
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);
        var commentFuture = findCommentById(commentId);

        return CompletableFuture.allOf(accountFuture, commentFuture).thenCompose(v -> {
                    var account = accountFuture.join();
                    var comment = commentFuture.join();

                    if (!account.equals(comment.getAccount())) {
                        throw new AppException(ErrorCode.ACCOUNT_COMMENT_NOT_MATCH);
                    }

                    commentMapper.updateComment(comment, request);

                    return CompletableFuture.completedFuture(commentRepository.save(comment));
                })
                .thenApply(commentMapper::toCommentResponse);
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('USER')")
    public CompletableFuture<CommentResponse> deleteCommentForUser(UUID commentId) {
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);
        var commentFuture = findCommentById(commentId);

        return CompletableFuture.allOf(accountFuture, commentFuture).thenCompose(v -> {
                    var account = accountFuture.join();
                    var comment = commentFuture.join();

                    if (!account.equals(comment.getAccount())) {
                        throw new AppException(ErrorCode.ACCOUNT_COMMENT_NOT_MATCH);
                    }

                    deleteRepliesRecursively(comment);
                    if (comment.getParentComment() != null) {
                        comment.getParentComment().getReplies().remove(comment);
                    }
                    commentRepository.delete(comment);

                    return CompletableFuture.completedFuture(comment);
                })
                .thenApply(commentMapper::toCommentResponse);
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public CompletableFuture<CommentResponse> deleteCommentForAdminAndStaff(UUID commentId) {
        var commentFuture = findCommentById(commentId);

        return CompletableFuture.allOf(commentFuture).thenCompose(v -> {
                    var comment = commentFuture.join();

                    deleteRepliesRecursively(comment);
                    if (comment.getParentComment() != null) {
                        comment.getParentComment().getReplies().remove(comment);
                    }
                    commentRepository.delete(comment);

                    return CompletableFuture.completedFuture(comment);
                })
                .thenApply(commentMapper::toCommentResponse);
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<Post> findPostById(UUID postId) {
        return CompletableFuture.supplyAsync(() ->
                postRepository.findById(postId)
                        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND))
        );
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<Account> findAccountByUsername(String username) {
        return CompletableFuture.supplyAsync(() ->
                accountRepository.findByUsername(username)
                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND))
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
    private CompletableFuture<Comment> findCommentById(UUID commentId) {
        return CompletableFuture.supplyAsync(() ->
                commentRepository.findById(commentId)
                        .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND))
        );
    }

    public String getUsernameFromJwt() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaim("username");
        }
        return null;
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<Account>> getBlockedAccountListByUsername(String username) {
        var accountFuture = findAccountByUsername(username);

        return accountFuture.thenApply(account -> {
            var blockedAccountEntityList = blockedAccountRepository.findByBlocker(account);

            return blockedAccountEntityList.stream()
                    .map(BlockedAccount::getBlocked)
                    .toList();
        });
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<Comment>> getAllComments() {
        return CompletableFuture.supplyAsync(() ->
                commentRepository.findAll().stream()
                        .toList());
    }

    private void deleteRepliesRecursively(Comment comment) {
        List<Comment> replies = comment.getReplies();

        if (replies != null && !replies.isEmpty()) {
            for (Comment reply : replies) {
                deleteRepliesRecursively(reply);
                commentRepository.delete(reply);
            }
            replies.clear();
        }
    }

    private boolean isFollowing(Account currentAccount, Account postOwner) {
        if (postOwner == null) {
            return false;
        }
        return followRepository
                .findByFollowerAndFollowee(currentAccount, postOwner)
                .isPresent();
    }

    private boolean hasRole(Account account, String role) {
        return account.getRole().getName().equals(role);
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<DailyPoint> createDailyPointLog(Account account, Post post, TypeBonus typeBonus) {
        return dailyPointRepository
                .findByPostAndTypeBonus(post, typeBonus)
                .thenCompose(dailyPoint -> {
                    if (dailyPoint != null || account.getWallet() == null) {
                        return CompletableFuture.completedFuture(null);
                    }

                    DailyPoint newDailyPoint = new DailyPoint();
                    newDailyPoint.setCreatedDate(new Date());
                    newDailyPoint.setPoint(null);
                    newDailyPoint.setPost(post);
                    newDailyPoint.setAccount(account);
                    newDailyPoint.setTypeBonus(typeBonus);
                    newDailyPoint.setPointEarned(typeBonus.getPointBonus());

                    var walletFuture = addPointToWallet(account, typeBonus);
                    return walletFuture.thenApply(wallet -> {
                        if (wallet == null) {
                            System.out.println("This Account Doesn't Have Wallet. Continuing without adding points");
                        }

                        return dailyPointRepository.save(newDailyPoint);
                    });
                });
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<Wallet> addPointToWallet(Account account, TypeBonus typeBonus) {
        var walletFuture = walletRepository.findByAccount(account);

        return walletFuture.thenCompose(wallet -> {
            if (wallet == null) {
                return CompletableFuture.completedFuture(null);
            }

            var balance = wallet.getBalance();
            balance = balance + typeBonus.getPointBonus();
            wallet.setBalance(balance);

            return CompletableFuture.completedFuture(walletRepository.save(wallet));
        });
    }

    private boolean checkCommentContentSafe(String content) {
        try {
            return contentFilterUtil.isCommentContentSafe(content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<Account>> getBlockerAccountListByUsername(String username) {
        var accountFuture = findAccountByUsername(username);

        return accountFuture.thenApply(account -> {
            var blockedAccountEntityList = blockedAccountRepository.findByBlocked(account);

            return blockedAccountEntityList.stream()
                    .map(BlockedAccount::getBlocker)
                    .toList();
        });
    }
}
