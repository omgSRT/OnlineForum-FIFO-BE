package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.ImageRequest;
import com.FA24SE088.OnlineForum.dto.request.PostCreateRequest;
import com.FA24SE088.OnlineForum.dto.request.PostUpdateRequest;
import com.FA24SE088.OnlineForum.dto.response.PostGetByIdResponse;
import com.FA24SE088.OnlineForum.dto.response.PostResponse;
import com.FA24SE088.OnlineForum.dto.response.TopicNoCategoryResponse;
import com.FA24SE088.OnlineForum.entity.*;
import com.FA24SE088.OnlineForum.enums.PostStatus;
import com.FA24SE088.OnlineForum.enums.TransactionType;
import com.FA24SE088.OnlineForum.enums.UpdatePostStatus;
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
import org.hibernate.Hibernate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);
        var topicFuture = findTopicById(request.getTopicId());
        var tagFuture = findTagById(request.getTagId());
        var pointFuture = getPoint();

        return CompletableFuture.allOf(accountFuture, topicFuture, tagFuture, pointFuture)
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


                    newPost.setCommentList(new ArrayList<>());
                    newPost.setUpvoteList(new ArrayList<>());
                    newPost.setFeedbackList(new ArrayList<>());

                    return CompletableFuture.completedFuture(unitOfWork.getPostRepository().save(newPost));
                })
                .thenCompose(savedPost -> {
                    var account = accountFuture.join();

                    CompletableFuture<List<Image>> imageFuture = createImages(request, savedPost);
                    CompletableFuture<DailyPoint> dailyPointFuture = createDailyPointLog(account.getAccountId(), savedPost);
                    CompletableFuture<Wallet> walletFuture = addPointToWallet(account.getAccountId());

                    return CompletableFuture.allOf(imageFuture, dailyPointFuture, walletFuture)
                            .thenCompose(v -> {
                                var point = pointFuture.join().get(0);
                                var wallet = walletFuture.join();

                                return createTransaction(point.getPointPerPost(), TransactionType.CREDIT, wallet)
                                        .thenCompose(transaction -> {
                                            savedPost.setImageList(imageFuture.join());  // Set image list after creation
                                            return CompletableFuture.completedFuture(unitOfWork.getPostRepository().save(savedPost));
                                        });
                            });
                })
                .thenApply(postMapper::toPostResponse);
    }
    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<List<PostResponse>> getAllPosts(int page, int perPage,
                                                             UUID accountId,
                                                             String topicName,
                                                             String tagName,
                                                             PostStatus status) {
        var postListFuture = findAllPosts();
        var accountFuture = accountId != null
                            ? findAccountById(accountId)
                            : CompletableFuture.completedFuture(null);
        var topicFuture = unitOfWork.getTopicRepository().findByName(topicName);
        var tagFuture = unitOfWork.getTagRepository().findByName(tagName);

        return CompletableFuture.allOf(postListFuture, accountFuture, topicFuture, tagFuture).thenCompose(v -> {
            var postList = postListFuture.join();
            var account = accountFuture.join();
            var topic = topicFuture.join();
            var tag = tagFuture.join();

            var list = postList.stream()
                    .filter(post -> account == null || post.getAccount().equals(account))
                    .filter(post -> topic == null || post.getTopic().equals(topic))
                    .filter(post -> tag == null || post.getTag().equals(tag))
                    .filter(post -> status == null || post.getStatus().equals(status.name()))
                    .map(postMapper::toPostResponse)
                    .toList();

            var paginatedList = paginationUtils.convertListToPage(page, perPage, list);

            return CompletableFuture.completedFuture(paginatedList);
        });
    }
    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<PostGetByIdResponse> getPostById(UUID postId) {
        var postFuture = findPostById(postId);

        return postFuture.thenApply(postMapper::toPostGetByIdResponse);
    }
    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<PostResponse> updatePostById(UUID postId, PostUpdateRequest request) {
        var postFuture = findPostById(postId);

        return postFuture.thenCompose(post -> {
            var deleteImageListFuture = deleteImagesByPost(post);
            var createImageFuture = createImages(request, post);

            return CompletableFuture.allOf(deleteImageListFuture, createImageFuture).thenCompose(v -> {
                postMapper.updatePost(post, request);
                post.setImageList(createImageFuture.join());

                return CompletableFuture.completedFuture(post);
            })
                    .thenApply(postMapper::toPostResponse);
        });
    }
    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<PostResponse> deleteByChangingPostStatusById(UUID postId) {
        var postFuture = findPostById(postId);

        return postFuture.thenCompose(post -> {
            var dailyPoint = post.getDailyPoint();
            var pointEarned = dailyPoint.getPointEarned();
            var wallet = post.getAccount().getWallet();
            var balance = wallet.getBalance();

            pointEarned = -pointEarned;
            dailyPoint.setPointEarned(pointEarned);
            balance = balance + pointEarned;
            if(balance < 0) balance = 0;
            wallet.setBalance(balance);

            post.setStatus(PostStatus.DELETED.name());

            unitOfWork.getDailyPointRepository().save(dailyPoint);
            unitOfWork.getWalletRepository().save(wallet);

            return createTransaction(pointEarned, TransactionType.DEBIT, wallet)
                    .thenCompose(transaction ->
                            CompletableFuture.completedFuture(unitOfWork.getPostRepository().save(post)));
            })
                .thenApply(postMapper::toPostResponse);
    }
    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<PostResponse> updatePostStatusById(UUID postId, UpdatePostStatus status) {
        var postFuture = findPostById(postId);

        return postFuture.thenCompose(post -> {
                    post.setStatus(status.name());
                    return CompletableFuture.completedFuture(unitOfWork.getPostRepository().save(post));
                })
                .thenApply(postMapper::toPostResponse);
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
    private CompletableFuture<List<Image>> createImages(PostUpdateRequest request, Post savedPost){
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
    private CompletableFuture<List<Image>> deleteImagesByPost(Post savedPost){
        var imageListFuture = unitOfWork.getImageRepository().findByPost(savedPost);
        List<Image> deletedImageList = new ArrayList<>();

        return imageListFuture.thenCompose(imageList -> {
            if(imageList.isEmpty()){
                return CompletableFuture.completedFuture(null);
            }

            for(Image image : imageList){
                deletedImageList.add(image);
                unitOfWork.getImageRepository().delete(image);
            }

            return CompletableFuture.completedFuture(deletedImageList);
        });
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<DailyPoint> createDailyPointLog(UUID accountId, Post savedPost){
        var accountFuture = findAccountById(accountId);
        var totalPointFuture = countUserTotalPointAtAGivenDate(accountId, new Date());
        var pointFuture = getPoint();

        return CompletableFuture.allOf(accountFuture, totalPointFuture, pointFuture)
                .thenCompose(all -> {
                    var account = accountFuture.join();
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
                    newDailyPoint.setPost(savedPost);
                    newDailyPoint.setAccount(account);
                    if(totalPoint + point.getPointPerPost() > point.getMaxPoint()){
                        newDailyPoint.setPointEarned(0);
                    }
                    else{
                        newDailyPoint.setPointEarned(point.getPointPerPost());
                    }

                    var dailyPointFuture = findDailyPointByAccountAndPost(account, savedPost);

                    return dailyPointFuture.thenCompose(dailyPoint -> {
                        if(dailyPoint != null){
                            throw new AppException(ErrorCode.DAILY_POINT_ALREADY_EXIST);
                        }

                        return CompletableFuture.completedFuture(
                                unitOfWork.getDailyPointRepository().save(newDailyPoint));
                    });
                });
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<Wallet> addPointToWallet(UUID accountId){
        var walletFuture = unitOfWork.getWalletRepository().findByAccountAccountId(accountId);
        var pointFuture = getPoint();
        var totalPointFuture = countUserTotalPointAtAGivenDate(accountId, new Date());

        return CompletableFuture.allOf(walletFuture, pointFuture, totalPointFuture).thenCompose(all -> {
            var wallet = walletFuture.join();

            if(wallet == null){
                return CompletableFuture.completedFuture(null);
            }

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
    private CompletableFuture<Account> findAccountByUsername(String username) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getAccountRepository().findByUsername(username)
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
    private CompletableFuture<List<Post>> findAllPosts() {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getPostRepository().findAll()
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
    private CompletableFuture<Wallet> findWalletById(UUID walletId) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getWalletRepository().findById(walletId)
                        .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND))
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
    @Async("AsyncTaskExecutor")
    private CompletableFuture<Transaction> createTransaction(double point, TransactionType type, Wallet wallet){
        if(wallet == null){
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.supplyAsync(() -> {
            Transaction transaction = new Transaction();
            transaction.setType(type.name());
            transaction.setCreatedDate(new Date());
            transaction.setAmount(point);
            transaction.setWallet(wallet);

            return unitOfWork.getTransactionRepository().save(transaction);
        });
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<DailyPoint> findDailyPointByAccountAndPost(Account account, Post post){
        return unitOfWork.getDailyPointRepository().findByAccountAndPost(account, post);
    }
    @Async("AsyncTaskExecutor")
    public String getUsernameFromJwt() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaim("username");  // Get the "username" claim from the token
        }
        return null;
    }
}
