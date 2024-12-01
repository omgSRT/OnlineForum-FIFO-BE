package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.CommentCreateRequest;
import com.FA24SE088.OnlineForum.dto.request.CommentGetAllResponse;
import com.FA24SE088.OnlineForum.dto.request.CommentUpdateRequest;
import com.FA24SE088.OnlineForum.dto.request.ReplyCreateRequest;
import com.FA24SE088.OnlineForum.dto.response.CommentNoPostResponse;
import com.FA24SE088.OnlineForum.dto.response.CommentResponse;
import com.FA24SE088.OnlineForum.dto.response.PostResponse;
import com.FA24SE088.OnlineForum.dto.response.ReplyCreateResponse;
import com.FA24SE088.OnlineForum.entity.*;
import com.FA24SE088.OnlineForum.enums.PostStatus;
import com.FA24SE088.OnlineForum.enums.TypeBonusNameEnum;
import com.FA24SE088.OnlineForum.enums.WebsocketEventName;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.CommentMapper;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import com.FA24SE088.OnlineForum.utils.ContentFilterUtil;
import com.FA24SE088.OnlineForum.utils.PaginationUtils;
import com.FA24SE088.OnlineForum.utils.SocketIOUtil;
import com.corundumstudio.socketio.SocketIOServer;
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

import java.util.*;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class CommentService {
    UnitOfWork unitOfWork;
    CommentMapper commentMapper;
    PaginationUtils paginationUtils;
    SocketIOUtil socketIOUtil;
    ContentFilterUtil contentFilterUtil;

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<CommentResponse> createComment(CommentCreateRequest request){
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);
        var postFuture = findPostById(request.getPostId());

        return CompletableFuture.allOf(accountFuture, postFuture).thenCompose(v -> {
            var account = accountFuture.join();
            var post = postFuture.join();

            if(post.getStatus().equals(PostStatus.DRAFT.name()) || post.getStatus().equals(PostStatus.HIDDEN.name())){
                throw new AppException(ErrorCode.CANNOT_COMMENT_ON_DRAFT);
            }

            return unitOfWork.getCommentRepository().countByPost(post).thenCompose(commentCount -> {
                long amount = commentCount + 1;

                return unitOfWork.getTypeBonusRepository().findByNameAndQuantity(TypeBonusNameEnum.COMMENT.name(), amount)
                        .thenCompose(typeBonus -> {
                            if(typeBonus != null){
                                return createDailyPointLog(post.getAccount(), post, typeBonus)
                                        .thenCompose(dailyPoint -> {
                                            var check = checkCommentContentSafe(request.getContent());
                                            if(!check){
                                                throw new AppException(ErrorCode.INAPPROPRIATE_COMMENT);
                                            }

                                            Comment newComment = commentMapper.toComment(request);
                                            newComment.setAccount(account);
                                            newComment.setPost(post);
                                            newComment.setParentComment(null);
                                            newComment.setReplies(new ArrayList<>());

                                            var savedComment = unitOfWork.getCommentRepository().save(newComment);
                                            socketIOUtil.sendEventToAllClientInAServer(WebsocketEventName.NOTIFICATION.toString(),newComment);
                                            return CompletableFuture.completedFuture(savedComment);
                                        });
                            }
                            else{
                                var check = checkCommentContentSafe(request.getContent());
                                if(!check){
                                    throw new AppException(ErrorCode.INAPPROPRIATE_COMMENT);
                                }

                                Comment newComment = commentMapper.toComment(request);
                                newComment.setAccount(account);
                                newComment.setPost(post);
                                newComment.setParentComment(null);
                                newComment.setReplies(new ArrayList<>());

                                var savedComment = unitOfWork.getCommentRepository().save(newComment);
                                socketIOUtil.sendEventToAllClientInAServer(WebsocketEventName.NOTIFICATION.toString(),newComment);
                                return CompletableFuture.completedFuture(savedComment);
                            }
                        });
            });
        })
                .thenApply(commentMapper::toCommentResponse);
    }
    @Transactional
    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<ReplyCreateResponse> createReply(ReplyCreateRequest request){
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);
        var parentCommentFuture = findCommentById(request.getParentCommentId());

        return CompletableFuture.allOf(accountFuture, parentCommentFuture).thenCompose(v -> {
                    var account = accountFuture.join();
                    var parentComment = parentCommentFuture.join();
                    var post = parentComment.getPost();

                    if(post.getStatus().equals(PostStatus.DRAFT.name()) || post.getStatus().equals(PostStatus.HIDDEN.name())){
                        throw new AppException(ErrorCode.CANNOT_COMMENT_ON_DRAFT);
                    }

                    return unitOfWork.getCommentRepository().countByPost(post).thenCompose(commentCount -> {
                        long amount = commentCount + 1;

                        return unitOfWork.getTypeBonusRepository().findByNameAndQuantity(TypeBonusNameEnum.COMMENT.name(), amount)
                                .thenCompose(typeBonus -> {
                                    if(typeBonus != null){
                                        return createDailyPointLog(post.getAccount(), post, typeBonus)
                                                .thenCompose(dailyPoint -> {
                                                    var check = checkCommentContentSafe(request.getContent());
                                                    if(!check){
                                                        throw new AppException(ErrorCode.INAPPROPRIATE_COMMENT);
                                                    }

                                                    Comment newReply = commentMapper.toCommentFromReplyRequest(request);
                                                    newReply.setAccount(account);
                                                    newReply.setPost(post);
                                                    newReply.setParentComment(parentComment);
                                                    newReply.setReplies(new ArrayList<>());
                                                    socketIOUtil.sendEventToAllClientInAServer(WebsocketEventName.NOTIFICATION.toString(),newReply);
                                                    return CompletableFuture.completedFuture(unitOfWork.getCommentRepository().save(newReply));
                                                });
                                    }
                                    else{
                                        var check = checkCommentContentSafe(request.getContent());
                                        if(!check){
                                            throw new AppException(ErrorCode.INAPPROPRIATE_COMMENT);
                                        }

                                        Comment newReply = commentMapper.toCommentFromReplyRequest(request);
                                        newReply.setAccount(account);
                                        newReply.setPost(post);
                                        newReply.setParentComment(parentComment);
                                        newReply.setReplies(new ArrayList<>());
                                        socketIOUtil.sendEventToAllClientInAServer(WebsocketEventName.NOTIFICATION.toString(),newReply);
                                        return CompletableFuture.completedFuture(unitOfWork.getCommentRepository().save(newReply));
                                    }
                                });
                    });
                })
                .thenApply(commentMapper::toReplyCreateResponse);
    }
    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<List<CommentGetAllResponse>> getAllComments(int page, int perPage){
        return CompletableFuture.supplyAsync(() -> {
            var list = unitOfWork.getCommentRepository().findAll().stream()
                    .map(commentMapper::toCommentGetAllResponse)
                    .toList();
            return paginationUtils.convertListToPage(page, perPage, list);
        });
    }
    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<List<CommentResponse>> getAllCommentsFromOtherUser(int page, int perPage, UUID otherAccountId){
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
                    boolean isBlockedByOther = unitOfWork.getBlockedAccountRepository()
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
    public CompletableFuture<List<CommentNoPostResponse>> getAllCommentsByPost(int page, int perPage, UUID postId){
        var postFuture = findPostById(postId);

        return postFuture.thenApply(post -> {
            var commentList = unitOfWork.getCommentRepository().findAllByPostWithReplies(post);

            var list = commentList.stream()
                    .map(commentMapper::toCommentNoPostResponseWithReplies)
                    .toList();
            return paginationUtils.convertListToPage(page, perPage, list);
        });
    }
    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<List<CommentResponse>> getAllCommentsByAccount(int page, int perPage, UUID accountId){
        var accountFuture = findAccountById(accountId);

        return accountFuture.thenCompose(account ->
                unitOfWork.getCommentRepository().findByAccount(account).thenCompose(list -> {
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
    public CompletableFuture<CommentResponse> updateComment(UUID commentId, CommentUpdateRequest request){
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);
        var commentFuture = findCommentById(commentId);

        return CompletableFuture.allOf(accountFuture, commentFuture).thenCompose(v -> {
                    var account = accountFuture.join();
                    var comment = commentFuture.join();

                    if(!account.equals(comment.getAccount())){
                        throw new AppException(ErrorCode.ACCOUNT_COMMENT_NOT_MATCH);
                    }

                    commentMapper.updateComment(comment, request);

                    return CompletableFuture.completedFuture(unitOfWork.getCommentRepository().save(comment));
                })
                .thenApply(commentMapper::toCommentResponse);
    }
    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('USER')")
    public CompletableFuture<CommentResponse> deleteCommentForUser(UUID commentId){
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);
        var commentFuture = findCommentById(commentId);

        return CompletableFuture.allOf(accountFuture, commentFuture).thenCompose(v -> {
                    var account = accountFuture.join();
                    var comment = commentFuture.join();

                    if(!account.equals(comment.getAccount())){
                        throw new AppException(ErrorCode.ACCOUNT_COMMENT_NOT_MATCH);
                    }

                    deleteRepliesRecursively(comment);
                    if(comment.getParentComment() != null){
                        comment.getParentComment().getReplies().remove(comment);
                    }
                    unitOfWork.getCommentRepository().delete(comment);

                    return CompletableFuture.completedFuture(comment);
                })
                .thenApply(commentMapper::toCommentResponse);
    }
    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public CompletableFuture<CommentResponse> deleteCommentForAdminAndStaff(UUID commentId){
        var commentFuture = findCommentById(commentId);

        return CompletableFuture.allOf(commentFuture).thenCompose(v -> {
                    var comment = commentFuture.join();

                    deleteRepliesRecursively(comment);
                    if(comment.getParentComment() != null){
                        comment.getParentComment().getReplies().remove(comment);
                    }
                    unitOfWork.getCommentRepository().delete(comment);

                    return CompletableFuture.completedFuture(comment);
                })
                .thenApply(commentMapper::toCommentResponse);
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<Post> findPostById(UUID postId) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getPostRepository().findById(postId)
                        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND))
        );
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<Account> findAccountByUsername(String username) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getAccountRepository().findByUsername(username)
                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND))
        );
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<Account> findAccountById(UUID accountId) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getAccountRepository().findById(accountId)
                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND))
        );
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<Comment> findCommentById(UUID commentId) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getCommentRepository().findById(commentId)
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
            var blockedAccountEntityList = unitOfWork.getBlockedAccountRepository().findByBlocker(account);

            return blockedAccountEntityList.stream()
                    .map(BlockedAccount::getBlocked)
                    .toList();
        });
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<Comment>> getAllComments(){
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getCommentRepository().findAll().stream()
                    .toList());
    }
    private void deleteRepliesRecursively(Comment comment) {
        List<Comment> replies = comment.getReplies();

        if (replies != null && !replies.isEmpty()) {
            for (Comment reply : replies) {
                deleteRepliesRecursively(reply);
                unitOfWork.getCommentRepository().delete(reply);
            }
            replies.clear();
        }
    }
    private boolean isFollowing(Account currentAccount, Account postOwner) {
        if(postOwner == null){
            return false;
        }
        return unitOfWork.getFollowRepository()
                .findByFollowerAndFollowee(currentAccount, postOwner)
                .isPresent();
    }
    private boolean hasRole(Account account, String role) {
        return account.getRole().getName().equals(role);
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<DailyPoint> createDailyPointLog(Account account, Post post, TypeBonus typeBonus) {
        return unitOfWork.getDailyPointRepository()
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
                        if(wallet == null){
                            System.out.println("This Account Doesn't Have Wallet. Continuing without adding points");
                        }

                        return unitOfWork.getDailyPointRepository().save(newDailyPoint);
                    });
                });
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<Wallet> addPointToWallet(Account account, TypeBonus typeBonus){
        var walletFuture = unitOfWork.getWalletRepository().findByAccount(account);

        return walletFuture.thenCompose(wallet -> {
            if(wallet == null){
                return CompletableFuture.completedFuture(null);
            }

            var balance = wallet.getBalance();
            balance = balance + typeBonus.getPointBonus();
            wallet.setBalance(balance);

            return CompletableFuture.completedFuture(unitOfWork.getWalletRepository().save(wallet));
        });
    }
    private boolean checkCommentContentSafe(String content){
        try {
            return contentFilterUtil.isTextSafe(content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
