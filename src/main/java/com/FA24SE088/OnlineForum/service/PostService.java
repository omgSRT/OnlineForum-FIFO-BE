package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.ImageRequest;
import com.FA24SE088.OnlineForum.dto.request.PostCreateRequest;
import com.FA24SE088.OnlineForum.dto.response.PostResponse;
import com.FA24SE088.OnlineForum.dto.response.TopicNoCategoryResponse;
import com.FA24SE088.OnlineForum.entity.*;
import com.FA24SE088.OnlineForum.enums.PostStatus;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.ImageMapper;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@Service
public class PostService {
    final UnitOfWork unitOfWork;
    final PostMapper postMapper;
    final ImageMapper imageMapper;
    final PaginationUtils paginationUtils;

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
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
                    newPost.setStatus(PostStatus.PUBLIC.name());
                    newPost.setAccount(account);
                    newPost.setTopic(topic);
                    newPost.setTag(tag);

                    return CompletableFuture.completedFuture(unitOfWork.getPostRepository().save(newPost));
                })
                .thenCompose(savedPost -> {
                    CompletableFuture<List<Image>> imageFuture = createImages(request, savedPost);
                    CompletableFuture<DailyPoint> dailyPointFuture = createDailyPointLog(request.getAccountId(), savedPost);
                    CompletableFuture<Wallet> walletFuture = addPointToWallet(request.getAccountId());

                    return CompletableFuture.allOf(imageFuture, dailyPointFuture, walletFuture)
                            .thenApply(v -> savedPost);
                })
                .thenApply(postMapper::toPostResponse);
    }
    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<List<PostResponse>> getAllPosts(int page, int perPage) {
        var postListFuture = unitOfWork.getPostRepository().findByStatus(PostStatus.PUBLIC.name());

        return postListFuture.thenCompose((postList -> {
            var list = postList.stream()
                    .map(postMapper::toPostResponse)
                    .toList();

            var paginatedList = paginationUtils.convertListToPage(page, perPage, list);

            return CompletableFuture.completedFuture(paginatedList);
        });
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<Image>> createImages(PostCreateRequest request, Post savedPost){
        if (request.getImageUrlList() == null || request.getImageUrlList().isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        List<Image> imageList = new ArrayList<>();

        for(ImageRequest imageRequest : request.getImageUrlList()){
            Image newImage = imageMapper.toImage(imageRequest);
            newImage.setPost(savedPost);
            imageList.add(newImage);
            unitOfWork.getImageRepository().save(newImage);
        }

        return CompletableFuture.completedFuture(imageList);
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<DailyPoint> createDailyPointLog(UUID accountId, Post savedPost){
        var accountFuture = findAccountById(accountId);
        var postFuture = findPostById(savedPost.getPostId());
        var totalPointFuture = countUserTotalPointAtAGivenDate(accountId, new Date());
        var pointFuture = getPoint();

        return CompletableFuture.allOf(accountFuture, postFuture, totalPointFuture, pointFuture)
                .thenCompose(all -> {
                    var account = accountFuture.join();
                    var post = postFuture.join();
                    var totalPoint = totalPointFuture.join();
                    var pointList = pointFuture.join();
                    Point point;

                    if(pointList.isEmpty()){
                        throw new AppException(ErrorCode.POINT_NOT_FOUND);
                    }
                    point = pointList.get(0);

                    DailyPoint newDailyPoint = new DailyPoint();
                    newDailyPoint.setCreatedDate(new Date());
                    newDailyPoint.setPoint(point);
                    newDailyPoint.setPost(post);
                    newDailyPoint.setAccount(account);
                    if(totalPoint + point.getPointPerPost() > point.getMaxPoint()){
                        newDailyPoint.setPointEarned(0);
                    }
                    else{
                        newDailyPoint.setPointEarned(point.getPointPerPost());
                    }

                    return CompletableFuture.completedFuture(newDailyPoint);
                });
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<Wallet> addPointToWallet(UUID accountId){
        var walletFuture = unitOfWork.getWalletRepository().findByAccountAccountId(accountId);
        var pointFuture = getPoint();
        var totalPointFuture = countUserTotalPointAtAGivenDate(accountId, new Date());

        return CompletableFuture.allOf(walletFuture, pointFuture, totalPointFuture).thenCompose(all -> {
            var wallet = walletFuture.join();
            var point = pointFuture.join().get(0);
            var currentWalletBalance = wallet.getBalance();
            var totalPoint = totalPointFuture.join();

            if(totalPoint + point.getPointPerPost() < point.getMaxPoint())
                wallet.setBalance(currentWalletBalance + point.getPointPerPost());

            return CompletableFuture.completedFuture(unitOfWork.getWalletRepository().save(wallet));
        });
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
    @Async("AsyncTaskExecutor")
    private CompletableFuture<Post> findPostById(UUID postId) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getPostRepository().findById(postId)
                        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND))
        );
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<Point>> getPoint() {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getPointRepository().findAll().stream()
                        .toList());
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<Double> countUserTotalPointAtAGivenDate(UUID accountId, Date givenDate){
        var accountFuture = findAccountById(accountId);
        return accountFuture.thenCompose(account ->
                unitOfWork.getDailyPointRepository().findByAccountAndCreatedDate(account, givenDate)
                        .thenCompose(dailyPoints -> {
                            double totalCount = 0;
                            if(dailyPoints == null || dailyPoints.isEmpty()){
                                totalCount = 0;
                            }
                            else{
                                for(DailyPoint dailyPoint : dailyPoints){
                                    totalCount += dailyPoint.getPointEarned();
                                }
                            }

                            return CompletableFuture.completedFuture(totalCount);
                        })
        );
    }
}
