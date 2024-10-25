package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.UpvoteRequest;
import com.FA24SE088.OnlineForum.dto.response.UpvoteCreateDeleteResponse;
import com.FA24SE088.OnlineForum.dto.response.UpvoteGetAllResponse;
import com.FA24SE088.OnlineForum.dto.response.UpvoteResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Post;
import com.FA24SE088.OnlineForum.entity.Upvote;
import com.FA24SE088.OnlineForum.enums.SuccessReturnMessage;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.UpvoteMapper;
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

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class UpvoteService {
    UnitOfWork unitOfWork;
    UpvoteMapper upvoteMapper;
    PaginationUtils paginationUtils;

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<UpvoteCreateDeleteResponse> addOrDeleteUpvote(UpvoteRequest request){
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);
        var postFuture = findPostById(request.getPostId());

        return CompletableFuture.allOf(accountFuture, postFuture).thenCompose(v -> {
            var account = accountFuture.join();
            var post = postFuture.join();
            var existUpvoteFuture = unitOfWork.getUpvoteRepository().findByPostAndAccount(post, account);

            return existUpvoteFuture.thenCompose(existUpvote -> {
                if(existUpvote != null){
                    unitOfWork.getUpvoteRepository().delete(existUpvote);
                    var upvoteResponse = upvoteMapper.toUpvoteCreateDeleteResponse(existUpvote);
                    upvoteResponse.setMessage(SuccessReturnMessage.DELETE_SUCCESS.getMessage());
                    return CompletableFuture.completedFuture(upvoteResponse);
                }

                Upvote newUpvote = new Upvote();
                newUpvote.setAccount(account);
                newUpvote.setPost(post);

                var upvoteResponse = upvoteMapper.toUpvoteCreateDeleteResponse(unitOfWork.getUpvoteRepository().save(newUpvote));
                upvoteResponse.setMessage(SuccessReturnMessage.CREATE_SUCCESS.getMessage());
                return CompletableFuture.completedFuture(upvoteResponse);
            });
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<UpvoteGetAllResponse> getAllUpvoteByPostId(int page, int perPage, UUID postId){
        var postFuture = findPostById(postId);

        return postFuture.thenCompose(post -> {
            var upvoteListFuture = unitOfWork.getUpvoteRepository().findByPost(post);

            return upvoteListFuture.thenApply(upvoteList -> {
                var convertResponse = upvoteList.stream()
                        .map(upvoteMapper::toUpvoteNoPostResponse)
                        .toList();
                var paginatedList = paginationUtils.convertListToPage(page, perPage, convertResponse);

                UpvoteGetAllResponse getAllResponse = new UpvoteGetAllResponse();
                getAllResponse.setCount(paginatedList.size());
                getAllResponse.setNoPostResponseList(paginatedList);

                return getAllResponse;
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
    private CompletableFuture<Account> findAccountByUsername(String username) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getAccountRepository().findByUsername(username)
                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND))
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
