package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.FeedbackRequest;
import com.FA24SE088.OnlineForum.dto.response.FeedbackResponse;
import com.FA24SE088.OnlineForum.entity.Feedback;
import com.FA24SE088.OnlineForum.entity.Post;
import com.FA24SE088.OnlineForum.enums.FeedbackStatus;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.FeedbackMapper;
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
public class FeedbackService {
    final UnitOfWork unitOfWork;
    final FeedbackMapper feedbackMapper;
    final PaginationUtils paginationUtils;

    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    @Async("AsyncTaskExecutor")
    public CompletableFuture<FeedbackResponse> createFeedback(FeedbackRequest request){
        var postFuture = findPostById(request.getPostId());

        return postFuture.thenCompose(post -> {
            Feedback newFeedback = feedbackMapper.toFeedback(request);
            newFeedback.setStatus(FeedbackStatus.PENDING.name());
            newFeedback.setPost(post);

            return CompletableFuture.completedFuture(unitOfWork.getFeedbackRepository().save(newFeedback));
        })
                .thenApply(feedbackMapper::toFeedbackResponse);
    }
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Async("AsyncTaskExecutor")
    public CompletableFuture<FeedbackResponse> updateFeedbackStatus(UUID feedbackId, FeedbackStatus status){
        var feedbackFuture = findFeedbackById(feedbackId);

        return feedbackFuture.thenCompose(feedback -> {
                    if(feedback.getStatus().equalsIgnoreCase(status.name())){
                        throw new AppException(ErrorCode.FEEDBACK_ALREADY_GOT_STATUS);
                    }

                    feedback.setStatus(status.name());

                    return CompletableFuture.completedFuture(unitOfWork.getFeedbackRepository().save(feedback));
                })
                .thenApply(feedbackMapper::toFeedbackResponse);
    }
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Async("AsyncTaskExecutor")
    public CompletableFuture<FeedbackResponse> deleteFeedbackById(UUID feedbackId){
        var feedbackFuture = findFeedbackById(feedbackId);

        return feedbackFuture.thenCompose(feedback -> {
                    unitOfWork.getFeedbackRepository().delete(feedback);

                    return CompletableFuture.completedFuture(feedback);
                })
                .thenApply(feedbackMapper::toFeedbackResponse);
    }
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Async("AsyncTaskExecutor")
    public CompletableFuture<List<FeedbackResponse>> getAllFeedback(int page, int perPage, UUID postId){
        var postFuture = postId != null
                ? findPostById(postId)
                : CompletableFuture.completedFuture(null);

        return postFuture.thenCompose(post -> {
                    var list = unitOfWork.getFeedbackRepository().findAll().stream()
                            .filter(feedback -> post == null || feedback.getPost().equals(post))
                            .map(feedbackMapper::toFeedbackResponse)
                            .toList();

                    var paginatedList = paginationUtils.convertListToPage(page, perPage, list);

                    return CompletableFuture.completedFuture(paginatedList);
                });
    }
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Async("AsyncTaskExecutor")
    public CompletableFuture<FeedbackResponse> getFeedbackById(UUID feedbackId){
        var feedbackFuture = findFeedbackById(feedbackId);

        return feedbackFuture.thenApply(feedbackMapper::toFeedbackResponse);
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<Post> findPostById(UUID postId) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getPostRepository().findById(postId)
                        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND))
        );
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<Feedback> findFeedbackById(UUID feedbackId) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getFeedbackRepository().findById(feedbackId)
                        .orElseThrow(() -> new AppException(ErrorCode.FEEDBACK_NOT_FOUND))
        );
    }
}
