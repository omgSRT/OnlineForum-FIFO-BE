package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.PostViewRequest;
import com.FA24SE088.OnlineForum.dto.response.PostViewResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Post;
import com.FA24SE088.OnlineForum.entity.PostView;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.PostViewMapper;
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
public class PostViewService {
    UnitOfWork unitOfWork;
    PostViewMapper postViewMapper;
    PaginationUtils paginationUtils;

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<PostViewResponse> createPostView(PostViewRequest request){
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);
        var postFuture = findPostById(request.getPostId());

        return CompletableFuture.allOf(accountFuture, postFuture).thenComposeAsync(ignored -> {
            Account account = accountFuture.join();
            Post post = postFuture.join();

            CompletableFuture<Boolean> viewExistsFuture = unitOfWork.getPostViewRepository()
                    .existsByPostAndAccount(post, account);

            return viewExistsFuture.thenCompose(viewExists -> {
                if (viewExists || isUserNotAllowedToView(account, post)) {
                    return CompletableFuture.completedFuture(null);
                }

                PostView newPostView = new PostView();
                newPostView.setPost(post);
                newPostView.setAccount(account);
                newPostView.setViewedDate(new Date());

                return CompletableFuture.supplyAsync(() -> {
                    PostView savedPostView = unitOfWork.getPostViewRepository().save(newPostView);
                    return postViewMapper.toPostViewResponse(savedPostView);
                });
            });
        });
    }
    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<List<PostViewResponse>> getAllPostView(int page, int perPage,
                                                                    UUID accountId,
                                                                    UUID postId){
        var accountFuture = accountId != null
                ? findAccountById(accountId)
                : CompletableFuture.completedFuture(null);
        var postFuture = postId != null
                ? findPostById(postId)
                : CompletableFuture.completedFuture(null);

        return CompletableFuture.allOf(accountFuture, postFuture).thenCompose(v -> {
            var account = accountFuture.join();
            var post = postFuture.join();

            CompletableFuture<List<PostView>> postViewListFuture = unitOfWork.getPostViewRepository()
                    .findAllByOrderByViewedDateDesc();

            return postViewListFuture.thenCompose(postViews -> {
                var list = postViews.stream()
                        .filter(postView -> account == null || postView.getAccount().equals(account))
                        .filter(postView -> post == null || postView.getPost().equals(post))
                        .map(postViewMapper::toPostViewResponse)
                        .toList();

                return CompletableFuture.completedFuture(paginationUtils.convertListToPage(page, perPage, list));
            });
        });
    }
    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<PostViewResponse> getPostViewById(UUID postViewId){
        var postViewFuture = findPostViewById(postViewId);

        return postViewFuture.thenApply(postViewMapper::toPostViewResponse);
    }
    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<PostViewResponse> deletePostViewById(UUID postViewId){
        var postViewFuture = findPostViewById(postViewId);

        return postViewFuture.thenCompose(postView -> {
            unitOfWork.getPostViewRepository().delete(postView);

            return CompletableFuture.completedFuture(postViewMapper.toPostViewResponse(postView));
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
    private CompletableFuture<Account> findAccountById(UUID accountId) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getAccountRepository().findById(accountId)
                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND))
        );
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<PostView> findPostViewById(UUID postViewId) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getPostViewRepository().findById(postViewId)
                        .orElseThrow(() -> new AppException(ErrorCode.POST_VIEW_NOT_FOUND))
        );
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<Account> findAccountByUsername(String username) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getAccountRepository().findByUsername(username)
                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND))
        );
    }
    private boolean isUserNotAllowedToView(Account account, Post post) {
        var role = account.getRole().getName();
        if (role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("STAFF")) {
            return true;
        }
        if (post.getAccount().equals(account)) {
            return true;
        }

        return false;
    }
    private String getUsernameFromJwt() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaim("username");
        }
        return null;
    }
}
