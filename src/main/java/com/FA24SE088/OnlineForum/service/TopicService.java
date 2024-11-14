package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.CategoryUpdateRequest;
import com.FA24SE088.OnlineForum.dto.request.TopicRequest;
import com.FA24SE088.OnlineForum.dto.request.TopicUpdateRequest;
import com.FA24SE088.OnlineForum.dto.response.*;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Category;
import com.FA24SE088.OnlineForum.entity.Topic;
import com.FA24SE088.OnlineForum.enums.SortOption;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class TopicService {
    UnitOfWork unitOfWork;
    PaginationUtils paginationUtils;
    TopicMapper topicMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<TopicResponse> createTopic(TopicRequest request){
        var categoryFuture = findCategoryById(request.getCategoryId());

        return categoryFuture.thenCompose(category ->
                unitOfWork.getTopicRepository().existsByNameAndCategory(request.getName(), category)
                        .thenCompose(exists -> {
                            if (exists) {
                                throw new AppException(ErrorCode.NAME_EXIST);
                            }

                            Topic newTopic = topicMapper.toTopic(request);
                            newTopic.setCategory(category);
                            newTopic.setPostList(new ArrayList<>());

                            return CompletableFuture.completedFuture(
                                    topicMapper.toTopicResponse(unitOfWork.getTopicRepository().save(newTopic))
                            );
                        }));
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<List<TopicResponse>> getAllTopics(int page, int perPage, UUID categoryId) {
        var categoryFuture = categoryId != null
                ? findCategoryById(categoryId)
                : CompletableFuture.completedFuture(null);

        return categoryFuture.thenCompose(category -> {
            var list = unitOfWork.getTopicRepository().findAll().stream()
                    .filter(topic -> category == null || topic.getCategory().equals(category))
                    .map(topicMapper::toTopicResponse)
                    .toList();

            var paginatedList = paginationUtils.convertListToPage(page, perPage, list);

            return CompletableFuture.completedFuture(paginatedList);
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    @Transactional(readOnly = true)
    public CompletableFuture<List<PopularTopicResponse>> getAllPopularTopics(int page, int perPage, SortOption sortOption) {
        return CompletableFuture.supplyAsync(() -> {
            List<CompletableFuture<PopularTopicResponse>> responseFutures = unitOfWork.getTopicRepository().findAll().stream()
                    .map(topic -> {
                        CompletableFuture<Integer> postCountFuture = unitOfWork.getPostRepository().countByTopic(topic);
                        CompletableFuture<Integer> upvoteCountFuture = unitOfWork.getUpvoteRepository().countByPostTopic(topic);
                        CompletableFuture<Integer> commentCountFuture = unitOfWork.getCommentRepository().countByPostTopic(topic);
                        CompletableFuture<Integer> viewCountFuture = unitOfWork.getPostViewRepository().countByPostTopic(topic);

                        return CompletableFuture.allOf(postCountFuture, upvoteCountFuture, commentCountFuture, viewCountFuture)
                                .thenApply(voidResult -> {
                                    PopularTopicResponse response = topicMapper.toPopularTopicResponse(topic);
                                    response.setPostAmount(postCountFuture.join());
                                    response.setUpvoteAmount(upvoteCountFuture.join());
                                    response.setCommentAmount(commentCountFuture.join());
                                    response.setViewAmount(viewCountFuture.join());
                                    return response;
                                });
                    })
                    .toList();

                    return CompletableFuture.allOf(responseFutures.toArray(new CompletableFuture[0]))
                            .thenApply(voidResult ->{
                                if(sortOption.equals(SortOption.DESCENDING)){
                                    return responseFutures.stream()
                                            .map(CompletableFuture::join)
                                            .sorted(Comparator.comparingInt((PopularTopicResponse r) ->
                                                            r.getPostAmount() + r.getUpvoteAmount() + r.getCommentAmount() + r.getViewAmount())
                                                    .reversed())
                                            .toList();
                                }
                                else{
                                    return responseFutures.stream()
                                            .map(CompletableFuture::join)
                                            .sorted(Comparator.comparingInt((PopularTopicResponse r) ->
                                                            r.getPostAmount() + r.getUpvoteAmount() + r.getCommentAmount() + r.getViewAmount()))
                                            .toList();
                                }
                            });
        })
                .thenApply(list -> paginationUtils.convertListToPage(page, perPage, list.join()));
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
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
        return unitOfWork.getTopicRepository().existsByName(request.getName())
                .thenCompose(exists -> {
                    if (exists) {
                        throw new AppException(ErrorCode.NAME_EXIST);

                    }

                    return CompletableFuture.supplyAsync(() -> {
                        var topic = unitOfWork.getTopicRepository().findById(topicId)
                                .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));

                        request.setName(request.getName() == null || request.getName().isEmpty()
                                ? topic.getName()
                                : request.getName());
                        request.setImageUrl(request.getImageUrl() == null || request.getImageUrl().isEmpty()
                                ? topic.getImageUrl()
                                : request.getImageUrl());
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
