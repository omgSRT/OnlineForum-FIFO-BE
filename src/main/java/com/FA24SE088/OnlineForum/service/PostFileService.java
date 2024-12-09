package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.PostFileCreateRequest;
import com.FA24SE088.OnlineForum.dto.request.PostFileUpdateRequest;
import com.FA24SE088.OnlineForum.dto.response.PostFileResponse;
import com.FA24SE088.OnlineForum.entity.Post;
import com.FA24SE088.OnlineForum.entity.PostFile;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.PostFileMapper;
import com.FA24SE088.OnlineForum.repository.PostFileRepository;
import com.FA24SE088.OnlineForum.repository.PostRepository;
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
public class PostFileService {
    PostFileRepository postFileRepository;
    PostRepository postRepository;
    PostFileMapper postFileMapper;
    PaginationUtils paginationUtils;

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<PostFileResponse> createPostFile(PostFileCreateRequest request) {
        var postFuture = findPostById(request.getPostId());

        return postFuture.thenCompose(post -> {
            var newPostFile = postFileMapper.toPostFile(request);
            newPostFile.setPost(post);

            var savedPostFile = postFileRepository.save(newPostFile);
            return CompletableFuture.completedFuture(postFileMapper.toPostFileResponse(savedPostFile));
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<PostFileResponse> deletePostFile(UUID postFileId) {
        var postFileFuture = findPostFileById(postFileId);

        return postFileFuture.thenCompose(postFile -> {
            postFileRepository.delete(postFile);

            return CompletableFuture.completedFuture(postFileMapper.toPostFileResponse(postFile));
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<PostFileResponse> updatePostFile(UUID postFileId, PostFileUpdateRequest request) {
        var postFileFuture = findPostFileById(postFileId);

        return postFileFuture.thenCompose(postFile -> {
            request.setUrl(request.getUrl() == null || request.getUrl().isEmpty()
                    ? postFile.getUrl()
                    : request.getUrl());
            postFileMapper.updatePostFile(postFile, request);
            return CompletableFuture.completedFuture(postFileMapper
                    .toPostFileResponse(postFileRepository.save(postFile)));
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<List<PostFileResponse>> getAllPostLists(int page, int perPage, UUID postId) {
        var postFuture = postId != null
                ? findPostById(postId)
                : CompletableFuture.completedFuture(null);

        return postFuture.thenCompose(post -> {
            var list = postFileRepository.findAll().stream()
                    .filter(postFile -> post == null || postFile.getPost().equals(post))
                    .map(postFileMapper::toPostFileResponse)
                    .toList();

            var paginatedList = paginationUtils.convertListToPage(page, perPage, list);
            return CompletableFuture.completedFuture(paginatedList);
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<PostFileResponse> getPostFileById(UUID postFileId) {
        return findPostFileById(postFileId).thenApply(postFileMapper::toPostFileResponse);
    }

    @Async("AsyncTaskExecutor")
    public CompletableFuture<Post> findPostById(UUID postId) {
        return CompletableFuture.supplyAsync(() ->
                postRepository.findById(postId)
                        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND)));
    }

    @Async("AsyncTaskExecutor")
    public CompletableFuture<PostFile> findPostFileById(UUID postFileId) {
        return CompletableFuture.supplyAsync(() ->
                postFileRepository.findById(postFileId)
                        .orElseThrow(() -> new AppException(ErrorCode.POST_FILE_NOT_FOUND)));
    }
}
