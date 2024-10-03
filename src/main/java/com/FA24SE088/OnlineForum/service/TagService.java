package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.TagRequest;
import com.FA24SE088.OnlineForum.dto.request.TopicRequest;
import com.FA24SE088.OnlineForum.dto.request.TopicUpdateRequest;
import com.FA24SE088.OnlineForum.dto.response.TagResponse;
import com.FA24SE088.OnlineForum.dto.response.TopicNoCategoryResponse;
import com.FA24SE088.OnlineForum.dto.response.TopicResponse;
import com.FA24SE088.OnlineForum.entity.Tag;
import com.FA24SE088.OnlineForum.entity.Topic;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.TagMapper;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import com.FA24SE088.OnlineForum.utils.PaginationUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@Service
public class TagService {
    final UnitOfWork unitOfWork;
    final PaginationUtils paginationUtils;
    final TagMapper tagMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<TagResponse> createTag(TagRequest request){
        return unitOfWork.getTagRepository().existsByNameContaining(request.getName())
                .thenCompose(exists -> {
                    if (exists) {
                        throw new AppException(ErrorCode.NAME_EXIST);
                    }

                    Tag newTag = tagMapper.toTag(request);

                    return CompletableFuture.completedFuture(
                            tagMapper.toTagResponse(unitOfWork.getTagRepository().save(newTag))
                    );
                });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<List<TagResponse>> getAllTags(int page, int perPage) {
        return CompletableFuture.supplyAsync(() -> {
            var list = unitOfWork.getTagRepository().findAll().stream()
                    .map(tagMapper::toTagResponse)
                    .toList();

            return paginationUtils.convertListToPage(page, perPage, list);
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<List<TagResponse>> getAllTagsByName(int page, int perPage, String name) {
        return CompletableFuture.supplyAsync(() -> {
            List<TagResponse> list = new ArrayList<>();
            if(name == null || name.trim().isBlank()){
                list = unitOfWork.getTagRepository().findAll().stream()
                        .map(tagMapper::toTagResponse)
                        .toList();
            }
            else{
                list = unitOfWork.getTagRepository().findByNameContaining(name).join()
                        .stream()
                        .map(tagMapper::toTagResponse)
                        .toList();
            }

            return paginationUtils.convertListToPage(page, perPage, list);
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public CompletableFuture<TagResponse> getTagById(UUID tagId) {
        return CompletableFuture.supplyAsync(() -> {
            var tag = unitOfWork.getTagRepository().findById(tagId)
                    .orElseThrow(() -> new AppException(ErrorCode.TAG_NOT_FOUND));

            return tagMapper.toTagResponse(tag);
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<TagResponse> deleteTagById(UUID tagId){
        return CompletableFuture.supplyAsync(() -> {
            var tag = unitOfWork.getTagRepository().findById(tagId)
                    .orElseThrow(() -> new AppException(ErrorCode.TAG_NOT_FOUND));

            unitOfWork.getTagRepository().delete(tag);

            return tagMapper.toTagResponse(tag);
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<TagResponse> updateTagById(UUID tagId, TagRequest request) {
        return unitOfWork.getTagRepository().existsByNameContaining(request.getName())
                .thenCompose(exists -> {
                    if (exists) {
                        throw new AppException(ErrorCode.NAME_EXIST);
                    }

                    return CompletableFuture.supplyAsync(() -> {
                        var tag = unitOfWork.getTagRepository().findById(tagId)
                                .orElseThrow(() -> new AppException(ErrorCode.TAG_NOT_FOUND));

                        tagMapper.updateTag(tag, request);
                        return unitOfWork.getTagRepository().save(tag);
                    }).thenApply(tagMapper::toTagResponse);
                });
    }
}
