package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.*;
import com.FA24SE088.OnlineForum.dto.response.PostResponse;
import com.FA24SE088.OnlineForum.entity.*;
import com.FA24SE088.OnlineForum.enums.PostStatus;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class PostService {
    UnitOfWork unitOfWork;
    PostMapper postMapper;
    ImageMapper imageMapper;
    PaginationUtils paginationUtils;

    //region CRUD Completed Post
    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    @Scheduled(fixedRate = 1200000)
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
                    var linkFile = request.getLinkFile();

                    Post newPost = postMapper.toPost(request);
                    newPost.setLinkFile(linkFile == null || linkFile.trim().isEmpty() ? null : linkFile);
                    newPost.setCreatedDate(new Date());
                    newPost.setLastModifiedDate(new Date());
                    newPost.setStatus(PostStatus.PUBLIC.name());
                    newPost.setAccount(account);
                    newPost.setTopic(topic);
                    newPost.setTag(tag);

                    newPost.setCommentList(new ArrayList<>());
                    newPost.setUpvoteList(new ArrayList<>());
                    newPost.setReportList(new ArrayList<>());
                    newPost.setBookMarkList(new ArrayList<>());
                    newPost.setPostViewList(new ArrayList<>());

                    return CompletableFuture.completedFuture(unitOfWork.getPostRepository().save(newPost));
                })
                .thenCompose(savedPost -> {
                    var account = accountFuture.join();

                    CompletableFuture<List<Image>> imageFuture = createImages(request, savedPost);
                    CompletableFuture<DailyPoint> dailyPointFuture = createDailyPointLog(account.getAccountId(), savedPost);
                    CompletableFuture<Wallet> walletFuture = addPointToWallet(account.getAccountId());

                    return CompletableFuture.allOf(imageFuture, dailyPointFuture, walletFuture)
                            .thenCompose(v -> {
                                var dailyPoint = dailyPointFuture.join();

                                if(imageFuture.join() != null){
                                    savedPost.setImageList(imageFuture.join());
                                }
                                else{
                                    savedPost.setImageList(new ArrayList<>());
                                }

                                List<DailyPoint> dailyPointList = new ArrayList<>();
                                if (dailyPoint != null) {
                                    dailyPointList.add(dailyPoint);
                                }
                                savedPost.setDailyPointList(dailyPointList);

                                return CompletableFuture.completedFuture(unitOfWork.getPostRepository().save(savedPost));
                            })
                            .thenCompose(post -> {
                                CompletableFuture<Integer> upvoteCountFuture = unitOfWork.getUpvoteRepository()
                                        .countByPost(post);
                                CompletableFuture<Integer> commentCountFuture = unitOfWork.getCommentRepository()
                                        .countByPost(post);
                                CompletableFuture<Integer> viewCountFuture = unitOfWork.getPostViewRepository()
                                        .countByPost(post);

                                return CompletableFuture.allOf(upvoteCountFuture, commentCountFuture, viewCountFuture)
                                        .thenApply(voidResult -> {
                                            PostResponse response = postMapper.toPostResponse(post);
                                            response.setUpvoteCount(upvoteCountFuture.join());
                                            response.setCommentCount(commentCountFuture.join());
                                            response.setViewCount(viewCountFuture.join());
                                            return response;
                                        });
                            });
                });
    }
    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<List<PostResponse>> getAllPosts(int page, int perPage,
                                                             UUID accountId,
                                                             UUID topicId,
                                                             UUID tagId,
                                                             UUID categoryId,
                                                             List<PostStatus> statuses,
                                                             Boolean IsFolloweeIncluded) {
        //IsFolloweeIncluded được tạo nhằm để lọc các post mà user hiện tại follow

        var postListFuture = findAllPostsOrderByCreatedDateDesc();
        var accountFuture = accountId != null
                            ? findAccountById(accountId)
                            : CompletableFuture.completedFuture(null);
        var username = getUsernameFromJwt();
        var blockedListFuture = getBlockedAccountListByUsername(username);
        var categoryFuture = categoryId != null
                ? findCategoryById(categoryId)
                : CompletableFuture.completedFuture(null);
        var topicFuture = topicId != null
                ? findTopicById(topicId)
                : CompletableFuture.completedFuture(null);
        var tagFuture = tagId != null
                ? findTagById(tagId)
                : CompletableFuture.completedFuture(null);
        var followerListFuture = getFollowerList();

        return CompletableFuture.allOf(postListFuture, accountFuture, topicFuture,
                tagFuture, followerListFuture, blockedListFuture).thenCompose(v -> {
            var postList = postListFuture.join();
            var account = accountFuture.join();
            var category = (Category) categoryFuture.join();
            var topic = (Topic) topicFuture.join();
            var tag = tagFuture.join();
            List<Account> followerAccountList = followerListFuture.join();
            List<Account> blockedAccountList = blockedListFuture.join();

            if (topic != null && category != null && !topic.getCategory().equals(category)) {
                throw new AppException(ErrorCode.TOPIC_NOT_BELONG_TO_CATEGORY);
            }

            var responseFutures = new ArrayList<>(postList.stream()
                    .filter(post -> {
                        if (IsFolloweeIncluded == null) {
                            return true;
                        } else if (IsFolloweeIncluded) {
                            return followerAccountList.contains(post.getAccount());
                        } else {
                            return !followerAccountList.contains(post.getAccount());
                        }
                    })
                    .filter(post -> !blockedAccountList.contains(post.getAccount()))
                    .filter(post -> account == null || post.getAccount().equals(account))
                    .filter(post -> category == null
                            || (post.getTopic() != null &&post.getTopic().getCategory() != null
                                        && post.getTopic().getCategory().equals(category)))
                    .filter(post -> topic == null || (post.getTopic() != null && post.getTopic().equals(topic)))
                    .filter(post -> tag == null || (post.getTag() != null && post.getTag().equals(tag)))
                    .filter(post -> statuses == null || statuses.isEmpty() ||
                            (safeValueOf(post.getStatus()) != null && statuses.contains(safeValueOf(post.getStatus()))))
                    .map(post -> {
                        CompletableFuture<Integer> upvoteCountFuture = unitOfWork.getUpvoteRepository()
                                .countByPost(post);
                        CompletableFuture<Integer> commentCountFuture = unitOfWork.getCommentRepository()
                                .countByPost(post);
                        CompletableFuture<Integer> viewCountFuture = unitOfWork.getPostViewRepository()
                                .countByPost(post);

                        return CompletableFuture.allOf(upvoteCountFuture, commentCountFuture, viewCountFuture)
                                .thenApply(voidResult -> {
                                    PostResponse response = postMapper.toPostResponse(post);
                                    response.setUpvoteCount(upvoteCountFuture.join());
                                    response.setCommentCount(commentCountFuture.join());
                                    response.setViewCount(viewCountFuture.join());
                                    return response;
                                });
                    })
                    .toList());

            return CompletableFuture.allOf(responseFutures.toArray(new CompletableFuture[0]))
                    .thenApply(voidResult -> responseFutures.stream()
                            .map(CompletableFuture::join)
                            .toList()
                    )
                    .thenApply(list -> paginationUtils.convertListToPage(page, perPage, list));
        });
    }
    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public CompletableFuture<List<PostResponse>> getAllPostsForCurrentStaff(int page, int perPage,
                                                             UUID topicId,
                                                             UUID tagId,
                                                             UUID categoryId,
                                                             Boolean IsFolloweeIncluded) {
        //IsFolloweeIncluded được tạo nhằm để lọc các post mà user hiện tại follow

        var postListFuture = findAllPostsOrderByCreatedDateDesc();
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);
        var categoryFuture = categoryId != null
                ? findCategoryById(categoryId)
                : CompletableFuture.completedFuture(null);
        var topicFuture = topicId != null
                ? findTopicById(topicId)
                : CompletableFuture.completedFuture(null);
        var tagFuture = tagId != null
                ? findTagById(tagId)
                : CompletableFuture.completedFuture(null);
        var followerListFuture = getFollowerList();

        return CompletableFuture.allOf(postListFuture, accountFuture, topicFuture, tagFuture, followerListFuture).thenCompose(v -> {
            var postList = postListFuture.join();
            var account = accountFuture.join();
            var category = categoryFuture.join();
            var topic = topicFuture.join();
            var tag = tagFuture.join();
            List<Account> followerAccountList = followerListFuture.join();
            var manageCategoryListFuture = unitOfWork.getCategoryRepository().findByAccount(account);

            return manageCategoryListFuture.thenCompose(manageCategoryList -> {
                var manageTopicList = getAllTopicsFromCategoryList(manageCategoryList);

                var responseFutures = new ArrayList<>(postList.stream()
                        .filter(post -> {
                            if (IsFolloweeIncluded == null) {
                                return true;
                            } else if (IsFolloweeIncluded) {
                                return followerAccountList.contains(post.getAccount());
                            } else {
                                return !followerAccountList.contains(post.getAccount());
                            }
                        })
                        .filter(post -> {
                            if (topic == null) {
                                return manageTopicList.contains(post.getTopic());
                            }
                            return post.getTopic() != null && post.getTopic().equals(topic);
                        })
                        .filter(post -> {
                            if (category == null) {
                                return post.getTopic().getCategory() != null && manageCategoryList.contains(post.getTopic().getCategory());
                            }
                            return post.getTopic().getCategory() != null && post.getTopic().getCategory().equals(category);
                        })
                        .filter(post -> tag == null || (post.getTag() != null && post.getTag().equals(tag)))
                        .filter(post -> post.getStatus() != null
                                && post.getStatus().equals(PostStatus.PUBLIC.name()))
                        .map(post -> {
                            CompletableFuture<Integer> upvoteCountFuture = unitOfWork.getUpvoteRepository()
                                    .countByPost(post);
                            CompletableFuture<Integer> commentCountFuture = unitOfWork.getCommentRepository()
                                    .countByPost(post);
                            CompletableFuture<Integer> viewCountFuture = unitOfWork.getPostViewRepository()
                                    .countByPost(post);

                            return CompletableFuture.allOf(upvoteCountFuture, commentCountFuture, viewCountFuture)
                                    .thenApply(voidResult -> {
                                        PostResponse response = postMapper.toPostResponse(post);
                                        response.setUpvoteCount(upvoteCountFuture.join());
                                        response.setCommentCount(commentCountFuture.join());
                                        response.setViewCount(viewCountFuture.join());
                                        return response;
                                    });
                        })
                        .toList());

                return CompletableFuture.allOf(responseFutures.toArray(new CompletableFuture[0]))
                        .thenApply(voidResult -> responseFutures.stream()
                                .map(CompletableFuture::join)
                                .toList()
                        )
                        .thenApply(list -> paginationUtils.convertListToPage(page, perPage, list));
            });
        });
    }
    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<List<PostResponse>> getAllPostsForCurrentUser(int page, int perPage) {
        var postListFuture = findAllPostsOrderByCreatedDateDesc();
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);

        return CompletableFuture.allOf(postListFuture, accountFuture).thenCompose(v -> {
            var postList = postListFuture.join();
            var account = accountFuture.join();

            var responseFutures = new ArrayList<>(postList.stream()
                    .filter(post -> post.getAccount().equals(account))
                    .filter(post -> post.getStatus().equals(PostStatus.PUBLIC.name())
                            || post.getStatus().equals(PostStatus.PRIVATE.name()))
                    .map(post -> {
                        CompletableFuture<Integer> upvoteCountFuture = unitOfWork.getUpvoteRepository()
                                .countByPost(post);
                        CompletableFuture<Integer> commentCountFuture = unitOfWork.getCommentRepository()
                                .countByPost(post);
                        CompletableFuture<Integer> viewCountFuture = unitOfWork.getPostViewRepository()
                                .countByPost(post);

                        return CompletableFuture.allOf(upvoteCountFuture, commentCountFuture, viewCountFuture)
                                .thenApply(voidResult -> {
                                    PostResponse response = postMapper.toPostResponse(post);
                                    response.setUpvoteCount(upvoteCountFuture.join());
                                    response.setCommentCount(commentCountFuture.join());
                                    response.setViewCount(viewCountFuture.join());
                                    return response;
                                });
                    })
                    .toList());

            return CompletableFuture.allOf(responseFutures.toArray(new CompletableFuture[0]))
                    .thenApply(voidResult -> responseFutures.stream()
                            .map(CompletableFuture::join)
                            .toList()
                    )
                    .thenApply(list -> paginationUtils.convertListToPage(page, perPage, list));
        });
    }
    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<PostResponse> getPostById(UUID postId) {
        var postFuture = findPostById(postId);

        return postFuture.thenCompose(post -> {
            CompletableFuture<Integer> upvoteCountFuture = unitOfWork.getUpvoteRepository()
                    .countByPost(post);
            CompletableFuture<Integer> commentCountFuture = unitOfWork.getCommentRepository()
                    .countByPost(post);
            CompletableFuture<Integer> viewCountFuture = unitOfWork.getPostViewRepository()
                    .countByPost(post);

            return CompletableFuture.allOf(upvoteCountFuture, commentCountFuture, viewCountFuture)
                    .thenApply(voidResult -> {
                        PostResponse response = postMapper.toPostResponse(post);
                        response.setUpvoteCount(upvoteCountFuture.join());
                        response.setCommentCount(commentCountFuture.join());
                        response.setViewCount(viewCountFuture.join());
                        return response;
                    });
        });
    }
    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<PostResponse> updatePostById(UUID postId, PostUpdateRequest request) {
        var postFuture = findPostById(postId);
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);

        return CompletableFuture.allOf(postFuture, accountFuture).thenCompose(v -> {
            var post = postFuture.join();
            var account = accountFuture.join();

            if(!account.equals(post.getAccount())){
                throw new AppException(ErrorCode.ACCOUNT_NOT_THE_AUTHOR_OF_POST);
            }

            CompletableFuture<List<Image>> deleteImageListFuture = CompletableFuture.completedFuture(null);
            CompletableFuture<List<Image>> createImageFuture = CompletableFuture.completedFuture(null);
            if (request.getImageUrlList() != null && !request.getImageUrlList().isEmpty()) {
                deleteImageListFuture = deleteImagesByPost(post);
                createImageFuture = createImages(request, post);
            }

            CompletableFuture<List<Image>> finalCreateImageFuture = createImageFuture;
            return CompletableFuture.allOf(deleteImageListFuture, createImageFuture, finalCreateImageFuture).thenCompose(voidData -> {
                postMapper.updatePost(post, request);
                if (request.getImageUrlList() != null && !request.getImageUrlList().isEmpty()) {
                    post.setImageList(finalCreateImageFuture.join() != null ? finalCreateImageFuture.join() : new ArrayList<>());
                }
                post.setLinkFile(request.getLinkFile() == null || request.getLinkFile().trim().isEmpty()
                        ? null
                        : request.getLinkFile());
                post.setLastModifiedDate(new Date());

                return CompletableFuture.completedFuture(unitOfWork.getPostRepository().save(post));
            }).thenCompose(savedPost -> {
                CompletableFuture<Integer> upvoteCountFuture = unitOfWork.getUpvoteRepository()
                        .countByPost(savedPost);
                CompletableFuture<Integer> commentCountFuture = unitOfWork.getCommentRepository()
                        .countByPost(savedPost);
                CompletableFuture<Integer> viewCountFuture = unitOfWork.getPostViewRepository()
                        .countByPost(post);

                return CompletableFuture.allOf(upvoteCountFuture, commentCountFuture, viewCountFuture)
                        .thenApply(voidResult -> {
                            PostResponse response = postMapper.toPostResponse(savedPost);
                            response.setUpvoteCount(upvoteCountFuture.join());
                            response.setCommentCount(commentCountFuture.join());
                            response.setViewCount(viewCountFuture.join());
                            return response;
                        });
            });
        });
    }
    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<PostResponse> deleteByChangingPostStatusById(UUID postId) {
        var postFuture = findPostById(postId);
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);

        return CompletableFuture.allOf(postFuture, accountFuture).thenCompose(v -> {
            var account = accountFuture.join();
            var post = postFuture.join();
            var categoryPost = post.getTopic().getCategory();

            return unitOfWork.getCategoryRepository().findByAccount(account).thenCompose(categoryList -> {
                if (post.getStatus().equals(PostStatus.DRAFT.name())) {
                    throw new AppException(ErrorCode.DRAFT_POST_CANNOT_CHANGE_STATUS);
                }
                if (post.getStatus().equals(PostStatus.HIDDEN.name())){
                    throw new AppException(ErrorCode.POST_ALREADY_HIDDEN);
                }

                if (account.getRole().getName().equals("USER") &&
                        !account.equals(post.getAccount())) {
                    throw new AppException(ErrorCode.ACCOUNT_NOT_THE_AUTHOR_OF_POST);
                }
                if (account.getRole().getName().equals("STAFF") &&
                        !categoryList.contains(categoryPost)) {
                    throw new AppException(ErrorCode.STAFF_NOT_SUPERVISE_CATEGORY);
                }

                post.setStatus(PostStatus.HIDDEN.name());
                post.setLastModifiedDate(new Date());

                return CompletableFuture.completedFuture(unitOfWork.getPostRepository().save(post));
            });
        }).thenCompose(post -> {
            CompletableFuture<Integer> upvoteCountFuture = unitOfWork.getUpvoteRepository()
                    .countByPost(post);
            CompletableFuture<Integer> commentCountFuture = unitOfWork.getCommentRepository()
                    .countByPost(post);
            CompletableFuture<Integer> viewCountFuture = unitOfWork.getPostViewRepository()
                    .countByPost(post);

            return CompletableFuture.allOf(upvoteCountFuture, commentCountFuture, viewCountFuture)
                    .thenApply(voidResult -> {
                        PostResponse response = postMapper.toPostResponse(post);
                        response.setUpvoteCount(upvoteCountFuture.join());
                        response.setCommentCount(commentCountFuture.join());
                        response.setViewCount(viewCountFuture.join());
                        return response;
                    });
        });
    }
    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<PostResponse> updatePostStatusById(UUID postId, UpdatePostStatus status) {
        var postFuture = findPostById(postId);
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);

        return CompletableFuture.allOf(postFuture, accountFuture).thenCompose(v -> {
                    var account = accountFuture.join();
                    var post = postFuture.join();
                    var categoryPost = post.getTopic().getCategory();

                    return unitOfWork.getCategoryRepository().findByAccount(account).thenCompose(categoryList -> {
                        if(post.getStatus().equals(PostStatus.DRAFT.name())){
                            throw new AppException(ErrorCode.DRAFT_POST_CANNOT_CHANGE_STATUS);
                        }

                        if(account.getRole().getName().equals("USER") &&
                                !account.equals(post.getAccount())){
                            throw new AppException(ErrorCode.ACCOUNT_NOT_THE_AUTHOR_OF_POST);
                        }
                        if(account.getRole().getName().equals("STAFF") &&
                                !categoryList.contains(categoryPost)){
                            throw new AppException(ErrorCode.STAFF_NOT_SUPERVISE_CATEGORY);
                        }

                        post.setStatus(status.name());
                        post.setLastModifiedDate(new Date());
                        return CompletableFuture.completedFuture(unitOfWork.getPostRepository().save(post));
                    });
                }).thenCompose(post -> {
                    CompletableFuture<Integer> upvoteCountFuture = unitOfWork.getUpvoteRepository()
                            .countByPost(post);
                    CompletableFuture<Integer> commentCountFuture = unitOfWork.getCommentRepository()
                            .countByPost(post);
                    CompletableFuture<Integer> viewCountFuture = unitOfWork.getPostViewRepository()
                            .countByPost(post);

                    return CompletableFuture.allOf(upvoteCountFuture, commentCountFuture, viewCountFuture)
                            .thenApply(voidResult -> {
                                PostResponse response = postMapper.toPostResponse(post);
                                response.setUpvoteCount(upvoteCountFuture.join());
                                response.setCommentCount(commentCountFuture.join());
                                response.setViewCount(viewCountFuture.join());
                                return response;
                            });
                });
    }
    //endregion

    //region CRUD Draft
    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<PostResponse> createDraft(DraftCreateRequest request) {
        if(request == null || isAllDraftCreateRequestFieldsNull(request)){
            throw new AppException(ErrorCode.NULL_DRAFT);
        }

        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);
        var topicFuture = request.getTopicId() != null
                ? findTopicById(request.getTopicId())
                : CompletableFuture.completedFuture(null);
        var tagFuture = request.getTagId() != null
                ? findTagById(request.getTagId())
                : CompletableFuture.completedFuture(null);

        return CompletableFuture.allOf(accountFuture, topicFuture, tagFuture)
                .thenCompose(v -> {
                    var account = accountFuture.join();
                    var topic = topicFuture.join();
                    var tag = tagFuture.join();
                    var linkFile = request.getLinkFile();

                    Post newPost = postMapper.toPost(request);
                    newPost.setLinkFile(linkFile == null || linkFile.trim().isEmpty() ? null : linkFile);
                    newPost.setCreatedDate(new Date());
                    newPost.setLastModifiedDate(new Date());
                    newPost.setStatus(PostStatus.DRAFT.name());
                    newPost.setAccount(account);
                    newPost.setTopic(topic != null ? (Topic) topic : null);
                    newPost.setTag(tag != null ? (Tag) tag : null);

                    newPost.setCommentList(new ArrayList<>());
                    newPost.setUpvoteList(new ArrayList<>());
                    newPost.setReportList(new ArrayList<>());
                    newPost.setBookMarkList(new ArrayList<>());
                    newPost.setDailyPointList(new ArrayList<>());
                    newPost.setPostViewList(new ArrayList<>());

                    return CompletableFuture.completedFuture(unitOfWork.getPostRepository().save(newPost));
                })
                .thenCompose(savedPost -> {
                    CompletableFuture<List<Image>> imageFuture = createImages(request, savedPost);

                    return imageFuture.thenCompose(imageList -> {
                                if(imageFuture.join() != null){
                                    savedPost.setImageList(imageFuture.join());
                                }
                                else{
                                    savedPost.setImageList(new ArrayList<>());
                                }
                                return CompletableFuture.completedFuture(unitOfWork.getPostRepository().save(savedPost));
                            });
                }).thenCompose(post -> {
                    CompletableFuture<Integer> upvoteCountFuture = unitOfWork.getUpvoteRepository()
                            .countByPost(post);
                    CompletableFuture<Integer> commentCountFuture = unitOfWork.getCommentRepository()
                            .countByPost(post);
                    CompletableFuture<Integer> viewCountFuture = unitOfWork.getPostViewRepository()
                            .countByPost(post);

                    return CompletableFuture.allOf(upvoteCountFuture, commentCountFuture, viewCountFuture)
                            .thenApply(voidResult -> {
                                PostResponse response = postMapper.toPostResponse(post);
                                response.setUpvoteCount(upvoteCountFuture.join());
                                response.setCommentCount(commentCountFuture.join());
                                response.setViewCount(viewCountFuture.join());
                                return response;
                            });
                });
    }
    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public CompletableFuture<List<PostResponse>> getAllDrafts(int page, int perPage,
                                                             UUID accountId) {
        var postListFuture = findAllPostsOrderByCreatedDateDesc();
        var accountFuture = accountId != null
                ? findAccountById(accountId)
                : CompletableFuture.completedFuture(null);

        return CompletableFuture.allOf(postListFuture, accountFuture).thenCompose(v -> {
            var postList = postListFuture.join();
            var account = accountFuture.join();

            var responseFutures = new ArrayList<>(postList.stream()
                    .filter(post -> account == null || post.getAccount().equals(account))
                    .filter(post -> post.getStatus().equals(PostStatus.DRAFT.name()))
                    .map(post -> {
                        CompletableFuture<Integer> upvoteCountFuture = unitOfWork.getUpvoteRepository()
                                .countByPost(post);
                        CompletableFuture<Integer> commentCountFuture = unitOfWork.getCommentRepository()
                                .countByPost(post);
                        CompletableFuture<Integer> viewCountFuture = unitOfWork.getPostViewRepository()
                                .countByPost(post);

                        return CompletableFuture.allOf(upvoteCountFuture, commentCountFuture, viewCountFuture)
                                .thenApply(voidResult -> {
                                    PostResponse response = postMapper.toPostResponse(post);
                                    response.setUpvoteCount(upvoteCountFuture.join());
                                    response.setCommentCount(commentCountFuture.join());
                                    response.setViewCount(viewCountFuture.join());
                                    return response;
                                });
                    })
                    .toList());

            return CompletableFuture.allOf(responseFutures.toArray(new CompletableFuture[0]))
                    .thenApply(voidResult -> responseFutures.stream()
                            .map(CompletableFuture::join)
                            .toList()
                    )
                    .thenApply(list -> paginationUtils.convertListToPage(page, perPage, list));
        });
    }
    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<List<PostResponse>> getAllDraftsForCurrentUser(int page, int perPage) {
        var postListFuture = findAllPostsOrderByCreatedDateDesc();
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);

        return CompletableFuture.allOf(postListFuture, accountFuture).thenCompose(v -> {
            var postList = postListFuture.join();
            var account = accountFuture.join();

            var responseFutures = new ArrayList<>(postList.stream()
                    .filter(post -> post.getAccount().equals(account))
                    .filter(post -> post.getStatus().equals(PostStatus.DRAFT.name()))
                    .map(post -> {
                        CompletableFuture<Integer> upvoteCountFuture = unitOfWork.getUpvoteRepository()
                                .countByPost(post);
                        CompletableFuture<Integer> commentCountFuture = unitOfWork.getCommentRepository()
                                .countByPost(post);
                        CompletableFuture<Integer> viewCountFuture = unitOfWork.getPostViewRepository()
                                .countByPost(post);

                        return CompletableFuture.allOf(upvoteCountFuture, commentCountFuture, viewCountFuture)
                                .thenApply(voidResult -> {
                                    PostResponse response = postMapper.toPostResponse(post);
                                    response.setUpvoteCount(upvoteCountFuture.join());
                                    response.setCommentCount(commentCountFuture.join());
                                    response.setViewCount(viewCountFuture.join());
                                    return response;
                                });
                    })
                    .toList());

            return CompletableFuture.allOf(responseFutures.toArray(new CompletableFuture[0]))
                    .thenApply(voidResult -> responseFutures.stream()
                            .map(CompletableFuture::join)
                            .toList()
                    )
                    .thenApply(list -> paginationUtils.convertListToPage(page, perPage, list));
        });
    }
    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<PostResponse> updateDraftById(UUID draftId, DraftUpdateRequest request) {
        if(request == null || isAllDraftUpdateRequestFieldsNull(request)){
            throw new AppException(ErrorCode.NULL_DRAFT);
        }
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);
        var postFuture = findPostById(draftId);
        var topicFuture = request.getTopicId() != null
                ? findTopicById(request.getTopicId())
                : CompletableFuture.completedFuture(null);
        var tagFuture = request.getTagId() != null
                ? findTagById(request.getTagId())
                : CompletableFuture.completedFuture(null);

        return CompletableFuture.allOf(postFuture, topicFuture, tagFuture, accountFuture).thenCompose(v -> {
            var post = postFuture.join();
            var topic = topicFuture.join();
            var tag = tagFuture.join();
            var account = accountFuture.join();

            if(!account.equals(post.getAccount())){
                throw new AppException(ErrorCode.ACCOUNT_NOT_THE_AUTHOR_OF_POST);
            }

            if(!post.getStatus().equals(PostStatus.DRAFT.name())){
                throw new AppException(ErrorCode.COMPLETED_POST_CANNOT_BE_EDIT_IN_DRAFT);
            }

            var deleteImageListFuture = deleteImagesByPost(post);
            var createImageFuture = createImages(request, post);

            return CompletableFuture.allOf(deleteImageListFuture, createImageFuture).thenCompose(voidData -> {
                        postMapper.updateDraft(post, request);
                        post.setLinkFile(request.getLinkFile() == null || request.getLinkFile().trim().isEmpty()
                                ? null
                                : request.getLinkFile());
                        post.setTopic(topic != null ? (Topic) topic : null);
                        post.setTag(tag != null ? (Tag) tag : null);
                        if(createImageFuture.join() != null){
                            post.setImageList(createImageFuture.join());
                        }
                        else{
                            post.setImageList(new ArrayList<>());
                        }
                        post.setLastModifiedDate(new Date());

                        return CompletableFuture.completedFuture(unitOfWork.getPostRepository().save(post));
                    }).thenCompose(savedPost -> {
                        CompletableFuture<Integer> upvoteCountFuture = unitOfWork.getUpvoteRepository()
                                .countByPost(savedPost);
                        CompletableFuture<Integer> commentCountFuture = unitOfWork.getCommentRepository()
                                .countByPost(savedPost);
                        CompletableFuture<Integer> viewCountFuture = unitOfWork.getPostViewRepository()
                                .countByPost(post);

                        return CompletableFuture.allOf(upvoteCountFuture, commentCountFuture, viewCountFuture)
                                .thenApply(voidResult -> {
                                    PostResponse response = postMapper.toPostResponse(savedPost);
                                    response.setUpvoteCount(upvoteCountFuture.join());
                                    response.setCommentCount(commentCountFuture.join());
                                    response.setViewCount(viewCountFuture.join());
                                    return response;
                                });
                    });
        });
    }
    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<PostResponse> updateDraftToPostById(UUID draftId) {
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);
        var postFuture = findPostById(draftId);

        return CompletableFuture.allOf(accountFuture, postFuture).thenCompose(v -> {
            var account = accountFuture.join();
            var post = postFuture.join();

            if(isCurrentDraftFieldsNull(post)){
                throw new AppException(ErrorCode.MISSING_REQUIRED_FIELDS_IN_DRAFT);
            }
            if(!post.getStatus().equals(PostStatus.DRAFT.name())){
                throw new AppException(ErrorCode.COMPLETED_POST_CANNOT_BE_UPDATE_TO_POST);
            }
            if(post.getTag() == null || post.getTopic() == null){
                throw new AppException(ErrorCode.TYPE_OR_TOPIC_NOT_FOUND);
            }

            var dailyPointFuture = createDailyPointLog(account.getAccountId(), post);
            var walletFuture = addPointToWallet(account.getAccountId());
            var pointFuture = getPoint();

            return CompletableFuture.allOf(dailyPointFuture, walletFuture, pointFuture).thenCompose(voidReturnData -> {
                var dailyPoint = dailyPointFuture.join();

                List<DailyPoint> dailyPointList = new ArrayList<>();
                if (dailyPoint != null) {
                    dailyPointList.add(dailyPoint);
                }
                post.setDailyPointList(dailyPointList);
                post.setStatus(PostStatus.PUBLIC.name());
                post.setLastModifiedDate(new Date());
                return CompletableFuture.completedFuture(unitOfWork.getPostRepository().save(post));
            }).thenCompose(savedPost -> {
                CompletableFuture<Integer> upvoteCountFuture = unitOfWork.getUpvoteRepository()
                        .countByPost(savedPost);
                CompletableFuture<Integer> commentCountFuture = unitOfWork.getCommentRepository()
                        .countByPost(savedPost);
                CompletableFuture<Integer> viewCountFuture = unitOfWork.getPostViewRepository()
                        .countByPost(post);

                return CompletableFuture.allOf(upvoteCountFuture, commentCountFuture, viewCountFuture)
                        .thenApply(voidResult -> {
                            PostResponse response = postMapper.toPostResponse(savedPost);
                            response.setUpvoteCount(upvoteCountFuture.join());
                            response.setCommentCount(commentCountFuture.join());
                            response.setViewCount(viewCountFuture.join());
                            return response;
                        });
            });
        });
    }
    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<List<PostResponse>> deleteDraftsById(List<UUID> draftIds) {
        return CompletableFuture.supplyAsync(() ->
                draftIds.stream()
                        .map(draftId -> findPostById(draftId)
                                .thenApply(post -> {
                                    if (!post.getStatus().equals(PostStatus.DRAFT.name())) {
                                        throw new AppException(ErrorCode.POST_NOT_A_DRAFT);
                                    }
                                    return post;
                                }))
                        .toList()
        ).thenCompose(postFutures -> {
            CompletableFuture.allOf(postFutures.toArray(new CompletableFuture[0])).join();

            List<Post> draftList = postFutures.stream()
                    .map(CompletableFuture::join)
                    .toList();

            unitOfWork.getPostRepository().deleteAll(draftList);
            var responseFutures = draftList.stream()
                    .map(post -> {
                        CompletableFuture<Integer> upvoteCountFuture = unitOfWork.getUpvoteRepository()
                                .countByPost(post);
                        CompletableFuture<Integer> commentCountFuture = unitOfWork.getCommentRepository()
                                .countByPost(post);
                        CompletableFuture<Integer> viewCountFuture = unitOfWork.getPostViewRepository()
                                .countByPost(post);

                        return CompletableFuture.allOf(upvoteCountFuture, commentCountFuture, viewCountFuture)
                                .thenApply(voidResult -> {
                                    PostResponse response = postMapper.toPostResponse(post);
                                    response.setUpvoteCount(upvoteCountFuture.join());
                                    response.setCommentCount(commentCountFuture.join());
                                    response.setViewCount(viewCountFuture.join());
                                    return response;
                                });
                    })
                    .toList();

            return CompletableFuture.allOf(responseFutures.toArray(new CompletableFuture[0]))
                    .thenApply(voidResult -> responseFutures.stream()
                            .map(CompletableFuture::join)
                            .toList()
                    );
        });
    }
    //endregion

    //region Smaller Modules To Assist Main Modules
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
    private CompletableFuture<List<Image>> createImages(DraftCreateRequest request, Post savedPost){
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
    private CompletableFuture<List<Image>> createImages(DraftUpdateRequest request, Post savedPost){
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
    private CompletableFuture<DailyPoint> createDailyPointLog(UUID accountId, Post savedPost) {
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
                    newDailyPoint.setTypeBonus(null);
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
                                unitOfWork.getDailyPointRepository().save(newDailyPoint)
                        );
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
    private CompletableFuture<List<Account>> getBlockedAccountListByUsername(String username) {
        var accountFuture = findAccountByUsername(username);

        return accountFuture.thenApply(account -> {
            var blockedAccountEntityList = unitOfWork.getBlockedAccountRepository().findByBlocker(account);

            return blockedAccountEntityList.stream()
                    .map(BlockedAccount::getBlocked)
                    .toList();
        });
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<Topic> findTopicById(UUID topicId) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getTopicRepository().findById(topicId)
                        .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND))
        );
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<Category> findCategoryById(UUID categoryId) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getCategoryRepository().findById(categoryId)
                        .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND))
        );
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<Post>> findAllPostsOrderByCreatedDateDesc() {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getPostRepository().findAllByOrderByCreatedDateDesc()
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
        Calendar parsedDateCal = Calendar.getInstance();
        parsedDateCal.setTime(givenDate);
        parsedDateCal.set(Calendar.HOUR_OF_DAY, 0);
        parsedDateCal.set(Calendar.MINUTE, 0);
        parsedDateCal.set(Calendar.SECOND, 0);
        parsedDateCal.set(Calendar.MILLISECOND, 0);
        Date normalizedGivenDate = parsedDateCal.getTime();

        var accountFuture = findAccountById(accountId);
        return accountFuture.thenCompose(account ->
                unitOfWork.getDailyPointRepository().findByAccount(account) // Adjust repository method as needed
                        .thenCompose(dailyPoints -> {
                            double totalCount = dailyPoints.stream()
                                    .filter(dailyPoint -> {
                                        Calendar createdDateCal = Calendar.getInstance();
                                        createdDateCal.setTime(dailyPoint.getCreatedDate());
                                        createdDateCal.set(Calendar.HOUR_OF_DAY, 0);
                                        createdDateCal.set(Calendar.MINUTE, 0);
                                        createdDateCal.set(Calendar.SECOND, 0);
                                        createdDateCal.set(Calendar.MILLISECOND, 0);

                                        return createdDateCal.getTime().equals(normalizedGivenDate);
                                    })
                                    .mapToDouble(DailyPoint::getPointEarned)
                                    .sum();

                            return CompletableFuture.completedFuture(totalCount);
                        })
        );
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<DailyPoint> findDailyPointByAccountAndPost(Account account, Post post){
        return unitOfWork.getDailyPointRepository().findByAccountAndPost(account, post);
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<Account>> getFollowerList(){
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);
        return accountFuture.thenCompose(account -> {
            var followeeList = unitOfWork.getFollowRepository().findByFollower(account).stream()
                    .map(Follow::getFollowee)
                    .toList();

            return CompletableFuture.completedFuture(followeeList);
        });
    }
    private String getUsernameFromJwt() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaim("username");
        }
        return null;
    }
    private PostStatus safeValueOf(String status) {
        try {
            return PostStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    private boolean isAllDraftCreateRequestFieldsNull(DraftCreateRequest request) {
        return request.getTitle() == null &&
                request.getContent() == null &&
                request.getTopicId() == null &&
                request.getTagId() == null &&
                (request.getImageUrlList() == null || request.getImageUrlList().isEmpty());
    }
    private boolean isAllDraftUpdateRequestFieldsNull(DraftUpdateRequest request) {
        return request.getTitle() == null &&
                request.getContent() == null &&
                request.getTopicId() == null &&
                request.getTagId() == null &&
                (request.getImageUrlList() == null || request.getImageUrlList().isEmpty());
    }
    private boolean isCurrentDraftFieldsNull(Post currentDraft) {
        return currentDraft.getTitle() == null &&
                currentDraft.getContent() == null &&
                currentDraft.getTopic() == null &&
                currentDraft.getTag() == null;
    }
    private List<Topic> getAllTopicsFromCategoryList(List<Category> categoryList){
        if(categoryList == null || categoryList.isEmpty()){
            return new ArrayList<>();
        }
        List<Topic> topicList = new ArrayList<>();
        for(Category category : categoryList){
            List<Topic> categoryTopicList = unitOfWork.getTopicRepository().findByCategory(category);
            topicList.addAll(categoryTopicList);
        }

        return topicList;
    }
    //endregion
}