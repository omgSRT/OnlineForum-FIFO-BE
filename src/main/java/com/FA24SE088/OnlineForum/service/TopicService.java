package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.CategoryUpdateRequest;
import com.FA24SE088.OnlineForum.dto.request.TopicRequest;
import com.FA24SE088.OnlineForum.dto.request.TopicUpdateRequest;
import com.FA24SE088.OnlineForum.dto.response.CategoryNoAccountResponse;
import com.FA24SE088.OnlineForum.dto.response.CategoryResponse;
import com.FA24SE088.OnlineForum.dto.response.TopicNoCategoryResponse;
import com.FA24SE088.OnlineForum.dto.response.TopicResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Category;
import com.FA24SE088.OnlineForum.entity.Topic;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.TopicMapper;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
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
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@Service
public class TopicService {
    final UnitOfWork unitOfWork;
    final PaginationUtils paginationUtils;
    final TopicMapper topicMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<TopicResponse> createTopic(TopicRequest request){
        var categoryFuture = findCategoryById(request.getCategoryId());

        return categoryFuture.thenCompose(category ->
                unitOfWork.getTopicRepository().existsByNameContaining(request.getName())
                        .thenCompose(exists -> {
                            if (exists) {
                                throw new AppException(ErrorCode.NAME_EXIST);
                            }

                            Topic newTopic = topicMapper.toTopic(request);
                            newTopic.setCategory(category);

                            return CompletableFuture.completedFuture(
                                    topicMapper.toTopicResponse(unitOfWork.getTopicRepository().save(newTopic))
                            );
                        }));
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<List<TopicNoCategoryResponse>> getAllTopics(int page, int perPage) {
        return CompletableFuture.supplyAsync(() -> {
            var list = unitOfWork.getTopicRepository().findAll().stream()
                    .map(topicMapper::toTopicNoCategoryResponse)
                    .toList();

            return paginationUtils.convertListToPage(page, perPage, list);
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<List<TopicNoCategoryResponse>> getAllTopicsByCategoryId(int page, int perPage, UUID categoryId) {
        var categoryFuture = findCategoryById(categoryId);

        return categoryFuture.thenCompose(category ->
                unitOfWork.getTopicRepository()
                        .findByCategoryCategoryId(category.getCategoryId())
                        .thenApply(list -> {
                            var responses = list.stream()
                                    .map(topicMapper::toTopicNoCategoryResponse)
                                    .toList();
                            return paginationUtils.convertListToPage(page, perPage, responses);
                        }));
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public CompletableFuture<TopicResponse> getTopicById(UUID topicId) {
        return CompletableFuture.supplyAsync(() -> {
            var topic = unitOfWork.getTopicRepository().findById(topicId)
                    .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));

            return topicMapper.toTopicResponse(topic);
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<TopicResponse> deleteTopicById(UUID topicId){
        return CompletableFuture.supplyAsync(() -> {
            var topic = unitOfWork.getTopicRepository().findById(topicId)
                    .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));

            unitOfWork.getTopicRepository().delete(topic);

            return topicMapper.toTopicResponse(topic);
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<TopicResponse> updateTopicById(UUID topicId, TopicUpdateRequest request) {
        return unitOfWork.getTopicRepository().existsByNameContaining(request.getName())
                .thenCompose(exists -> {
                    if (exists) {
                        throw new AppException(ErrorCode.NAME_EXIST);
                    }

                    return CompletableFuture.supplyAsync(() -> {
                        var topic = unitOfWork.getTopicRepository().findById(topicId)
                                .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));

                        topicMapper.updateTopic(topic, request);
                        return unitOfWork.getTopicRepository().save(topic);
                    }).thenApply(topicMapper::toTopicResponse);
                });
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<Category> findCategoryById(UUID categoryId) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getCategoryRepository().findById(categoryId)
                        .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND))
        );
    }
}
