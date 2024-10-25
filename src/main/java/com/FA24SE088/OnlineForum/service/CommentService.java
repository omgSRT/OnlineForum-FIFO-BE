package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.CommentCreateRequest;
import com.FA24SE088.OnlineForum.dto.request.CommentGetAllResponse;
import com.FA24SE088.OnlineForum.dto.request.CommentUpdateRequest;
import com.FA24SE088.OnlineForum.dto.request.ReplyCreateRequest;
import com.FA24SE088.OnlineForum.dto.response.CommentNoPostResponse;
import com.FA24SE088.OnlineForum.dto.response.CommentResponse;
import com.FA24SE088.OnlineForum.dto.response.ReplyCreateResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Comment;
import com.FA24SE088.OnlineForum.entity.Post;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.CommentMapper;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import com.FA24SE088.OnlineForum.utils.PaginationUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class CommentService {
    UnitOfWork unitOfWork;
    CommentMapper commentMapper;
    PaginationUtils paginationUtils;

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<CommentResponse> createComment(CommentCreateRequest request){
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);
        var postFuture = findPostById(request.getPostId());

        return CompletableFuture.allOf(accountFuture, postFuture).thenCompose(v -> {
            var account = accountFuture.join();
            var post = postFuture.join();

            Comment newComment = commentMapper.toComment(request);
            newComment.setAccount(account);
            newComment.setPost(post);
            newComment.setParentComment(null);
            newComment.setReplies(new ArrayList<>());

            return CompletableFuture.completedFuture(unitOfWork.getCommentRepository().save(newComment));
        })
                .thenApply(commentMapper::toCommentResponse);
    }
    @Transactional
    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<ReplyCreateResponse> createReply(ReplyCreateRequest request){
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);
        var postFuture = findPostById(request.getPostId());
        var parentCommentFuture = findCommentById(request.getParentCommentId());

        return CompletableFuture.allOf(accountFuture, postFuture, parentCommentFuture).thenCompose(v -> {
                    var account = accountFuture.join();
                    var post = postFuture.join();
                    var parentComment = parentCommentFuture.join();

                    Comment newReply = commentMapper.toCommentFromReplyRequest(request);
                    newReply.setAccount(account);
                    newReply.setPost(post);
                    newReply.setParentComment(parentComment);
                    newReply.setReplies(new ArrayList<>());

                    return CompletableFuture.completedFuture(unitOfWork.getCommentRepository().save(newReply));
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
    @Transactional
    @Async("AsyncTaskExecutor")
    private CompletableFuture<Comment> findCommentById(UUID commentId) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getCommentRepository().findById(commentId)
                        .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND))
        );
    }
    @Async("AsyncTaskExecutor")
    public String getUsernameFromJwt() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaim("username");
        }
        return null;
    }
}
