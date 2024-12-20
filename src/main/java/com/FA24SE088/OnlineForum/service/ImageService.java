package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.ImageCreateRequest;
import com.FA24SE088.OnlineForum.dto.response.ImageResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.BlockedAccount;
import com.FA24SE088.OnlineForum.entity.Image;
import com.FA24SE088.OnlineForum.entity.Post;
import com.FA24SE088.OnlineForum.enums.PostStatus;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.ImageMapper;
import com.FA24SE088.OnlineForum.repository.*;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class ImageService {
    ImageRepository imageRepository;
    PostRepository postRepository;
    BlockedAccountRepository blockedAccountRepository;
    AccountRepository accountRepository;
    FollowRepository followRepository;
    ImageMapper imageMapper;
    PaginationUtils paginationUtils;

    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Async("AsyncTaskExecutor")
    public CompletableFuture<List<ImageResponse>> createImage(ImageCreateRequest request) {
        var postFuture = findPostById(request.getPostId());

        return postFuture.thenCompose(post -> {
            List<Image> imageList = new ArrayList<>();
            for (String url : request.getUrls()) {
                Image newImage = new Image();
                newImage.setUrl(url);
                newImage.setPost(post);
                imageRepository.save(newImage);
                imageList.add(newImage);
            }

            List<ImageResponse> imageResponseList = imageList.stream()
                    .map(imageMapper::toImageResponse)
                    .toList();

            return CompletableFuture.completedFuture(imageResponseList);
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<List<ImageResponse>> getAllImages(int page, int perPage, UUID postId) {
        var postFuture = postId != null
                ? findPostById(postId)
                : CompletableFuture.completedFuture(null);

        return postFuture.thenCompose(post -> {
            var list = imageRepository.findAll().stream()
                    .filter(image -> post == null || image.getPost().equals(post))
                    .map(imageMapper::toImageResponse)
                    .toList();

            var paginatedList = paginationUtils.convertListToPage(page, perPage, list);

            return CompletableFuture.completedFuture(paginatedList);
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<List<ImageResponse>> getAllImagesByCurrentAccount(int page, int perPage) {
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);

        return accountFuture.thenCompose(account ->
                postRepository.findByAccount(account)
                        .thenCompose(postList -> {
                            var list = postList.stream()
                                    .flatMap(post -> post.getImageList().stream())
                                    .map(imageMapper::toImageResponse)
                                    .toList();

                            var paginatedList = paginationUtils.convertListToPage(page, perPage, list);

                            return CompletableFuture.completedFuture(paginatedList);
                        })
        );
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<List<ImageResponse>> getAllImagesFromOtherUser(int page, int perPage, UUID otherAccountId) {
        var imageListResponse = getAllImages();
        var username = getUsernameFromJwt();
        var currentAccountFuture = findAccountByUsername(username);
        var otherAccountFuture = findAccountById(otherAccountId);
        var blockedListFuture = getBlockedAccountListByUsername(username);

        return CompletableFuture.allOf(imageListResponse, currentAccountFuture, otherAccountFuture, blockedListFuture)
                .thenCompose(v -> {
                    var imageList = imageListResponse.join();
                    var currentAccount = currentAccountFuture.join();
                    var otherAccount = otherAccountFuture.join();
                    var blockedAccountList = blockedListFuture.join();

                    boolean isBlockedByCurrent = blockedAccountList.contains(otherAccount);
                    boolean isBlockedByOther = blockedAccountRepository
                            .findByBlockerAndBlocked(otherAccount, currentAccount).isPresent();
                    boolean isFollowing = isFollowing(currentAccount, otherAccount);
                    boolean isAuthor = currentAccount.equals(otherAccount);
                    boolean isStaffOrAdmin = hasRole(currentAccount, "ADMIN") || hasRole(currentAccount, "STAFF");

                    var responses = imageList.stream()
                            .filter(image -> image.getPost() != null && image.getPost().getAccount().equals(otherAccount))
                            .filter(image -> isAuthor || isStaffOrAdmin
                                    || image.getPost().getStatus().equals(PostStatus.PUBLIC.name())
                                    || (image.getPost().getStatus().equals(PostStatus.PRIVATE.name()) && isFollowing))
                            .filter(image -> !isBlockedByCurrent && !isBlockedByOther)
                            .map(imageMapper::toImageResponse)
                            .toList();

                    var paginatedList = paginationUtils.convertListToPage(page, perPage, responses);

                    return CompletableFuture.completedFuture(paginatedList);
                });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<ImageResponse> getImageById(UUID imageId) {
        var imageFuture = findImageById(imageId);

        return imageFuture.thenApply(imageMapper::toImageResponse);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Async("AsyncTaskExecutor")
    public CompletableFuture<ImageResponse> deleteImageById(UUID imageId) {
        var imageFuture = findImageById(imageId);

        return imageFuture.thenCompose(image -> {
                    imageRepository.delete(image);

                    return CompletableFuture.completedFuture(image);
                })
                .thenApply(imageMapper::toImageResponse);
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<Post> findPostById(UUID postId) {
        return CompletableFuture.supplyAsync(() ->
                postRepository.findById(postId)
                        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND))
        );
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<Account> findAccountById(UUID accountId) {
        return CompletableFuture.supplyAsync(() ->
                accountRepository.findById(accountId)
                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND))
        );
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<Image> findImageById(UUID imageId) {
        return CompletableFuture.supplyAsync(() ->
                imageRepository.findById(imageId)
                        .orElseThrow(() -> new AppException(ErrorCode.IMAGE_NOT_FOUND))
        );
    }

    @Async("AsyncTaskExecutor")
    public String getUsernameFromJwt() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaim("username");  // Get the "username" claim from the token
        }
        return null;
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<Account> findAccountByUsername(String username) {
        return CompletableFuture.supplyAsync(() ->
                accountRepository.findByUsername(username)
                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND))
        );
    }

    @Async("AsyncTaskExecutor")
    public CompletableFuture<List<Image>> getAllImages() {
        return CompletableFuture.supplyAsync(() -> imageRepository.findAll().stream()
                .toList());
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<Account>> getBlockedAccountListByUsername(String username) {
        var accountFuture = findAccountByUsername(username);

        return accountFuture.thenApply(account -> {
            var blockedAccountEntityList = blockedAccountRepository.findByBlocker(account);

            return blockedAccountEntityList.stream()
                    .map(BlockedAccount::getBlocked)
                    .toList();
        });
    }

    private boolean isFollowing(Account currentAccount, Account postOwner) {
        if (postOwner == null) {
            return false;
        }
        return followRepository
                .findByFollowerAndFollowee(currentAccount, postOwner)
                .isPresent();
    }

    private boolean hasRole(Account account, String role) {
        return account.getRole().getName().equals(role);
    }
}
