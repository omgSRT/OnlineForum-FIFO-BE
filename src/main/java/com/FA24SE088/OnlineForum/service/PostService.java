package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.CategoryRequest;
import com.FA24SE088.OnlineForum.dto.request.PostCreateRequest;
import com.FA24SE088.OnlineForum.dto.response.CategoryResponse;
import com.FA24SE088.OnlineForum.dto.response.PostResponse;
import com.FA24SE088.OnlineForum.entity.*;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.PostMapper;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import com.FA24SE088.OnlineForum.utils.PaginationUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@Service
public class PostService {
    final UnitOfWork unitOfWork;
    final PostMapper postMapper;
    final PaginationUtils paginationUtils;

    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<PostResponse> createPost(PostCreateRequest request) {
        var accountFuture = findAccountById(request.getAccountId());
        var topicFuture = findTopicById(request.getTopicId());
        var tagFuture = findTagById(request.getTagId());

        return CompletableFuture.allOf(accountFuture, topicFuture, tagFuture)
                .thenCompose(v -> {
                    var account = accountFuture.join();
                    var topic = topicFuture.join();
                    var tag = tagFuture.join();

                    Post newPost = postMapper.toPost(request);
                    newPost.setCreatedDate(new Date());
                    newPost.setLastModifiedDate(new Date());
                    newPost.setAccount(account);
                    newPost.setTopic(topic);
                    newPost.setTag(tag);

                    return CompletableFuture.completedFuture(unitOfWork.getPostRepository().save(newPost));
                })
                .thenApply(postMapper::toPostResponse);
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<Account> findAccountById(UUID accountId) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getAccountRepository().findById(accountId)
                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND))
        );
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<Topic> findTopicById(UUID topicId) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getTopicRepository().findById(topicId)
                        .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND))
        );
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<Tag> findTagById(UUID tagId) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getTagRepository().findById(tagId)
                        .orElseThrow(() -> new AppException(ErrorCode.TAG_NOT_FOUND))
        );
    }
}
