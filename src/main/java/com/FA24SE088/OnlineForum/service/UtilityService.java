package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.response.FilterTransactionResponse;
import com.FA24SE088.OnlineForum.dto.response.SearchEverythingResponse;
import com.FA24SE088.OnlineForum.dto.response.TransactionResponse;
import com.FA24SE088.OnlineForum.entity.*;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.TransactionMapper;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class UtilityService {
    TransactionMapper transactionMapper;
    UnitOfWork unitOfWork;

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<SearchEverythingResponse> searchEverything(String keyword,
                                                                        boolean isOnlyAccountIncluded,
                                                                        boolean isOnlyCategoryIncluded,
                                                                        boolean isOnlyTopicIncluded,
                                                                        boolean isOnlyPostIncluded) {
        if (keyword == null || keyword.length() <= 1) {
            throw new AppException(ErrorCode.KEYWORD_LENGTH_SHORTER_THAN_ONE);
        }

        boolean includeAll = !(isOnlyAccountIncluded || isOnlyCategoryIncluded || isOnlyTopicIncluded || isOnlyPostIncluded);


        CompletableFuture<List<Account>> accountListByUsernameFuture = isOnlyAccountIncluded || includeAll
                ? findAllAccountsByUsernameContainingIgnoreCase(keyword)
                : null;
        CompletableFuture<List<Account>> accountListByEmailFuture = isOnlyAccountIncluded || includeAll
                ? findAllAccountsByEmailContainingIgnoreCase(keyword)
                : null;
        CompletableFuture<List<Category>> categoryListByNameFuture = isOnlyCategoryIncluded || includeAll
                ? findAllCategoriesByNameContainingIgnoreCase(keyword)
                : null;
        CompletableFuture<List<Topic>> topicListByNameFuture = isOnlyTopicIncluded || includeAll
                ? findAllTopicsByNameContainingIgnoreCase(keyword)
                : null;
        CompletableFuture<List<Post>> postListByTitleFuture = isOnlyPostIncluded || includeAll
                ? findAllPostsByTitleContainingIgnoreCase(keyword)
                : null;
        CompletableFuture<List<Post>> postListByContentFuture = isOnlyPostIncluded || includeAll
                ? findAllPostsByContentContainingIgnoreCase(keyword)
                : null;
        CompletableFuture<?>[] futures = Stream.of(accountListByUsernameFuture, categoryListByNameFuture,
                        topicListByNameFuture, postListByTitleFuture, postListByContentFuture)
                .filter(Objects::nonNull)
                .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(futures).thenApply(v -> {
            List<Account> combinedAccountList = collectAccounts(accountListByUsernameFuture, accountListByEmailFuture);
            List<Category> categoryListByName = categoryListByNameFuture != null ? categoryListByNameFuture.join() : Collections.emptyList();
            List<Topic> topicListByName = topicListByNameFuture != null ? topicListByNameFuture.join() : Collections.emptyList();
            List<Post> combinedPostList = collectPosts(postListByTitleFuture, postListByContentFuture);

            return new SearchEverythingResponse(combinedAccountList, categoryListByName, topicListByName, combinedPostList);
        });
    }


    private List<Account> collectAccounts(CompletableFuture<List<Account>> usernameFuture,
                                          CompletableFuture<List<Account>> emailFuture) {
        List<Account> usernameAccounts = usernameFuture != null ? usernameFuture.join() : Collections.emptyList();
        List<Account> emailAccounts = emailFuture != null ? emailFuture.join() : Collections.emptyList();
        return Stream.concat(usernameAccounts.stream(), emailAccounts.stream()).distinct().toList();
    }

    private List<Post> collectPosts(CompletableFuture<List<Post>> titleFuture, CompletableFuture<List<Post>> contentFuture) {
        List<Post> titlePosts = titleFuture != null ? titleFuture.join() : Collections.emptyList();
        List<Post> contentPosts = contentFuture != null ? contentFuture.join() : Collections.emptyList();
        return Stream.concat(titlePosts.stream(), contentPosts.stream()).distinct().toList();
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<Account>> findAllAccountsByUsernameContainingIgnoreCase(String username) {
        return unitOfWork.getAccountRepository().findByUsernameContainingIgnoreCase(username);
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<Account>> findAllAccountsByEmailContainingIgnoreCase(String email) {
        return unitOfWork.getAccountRepository().findByEmailContainingIgnoreCase(email);
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<Category>> findAllCategoriesByNameContainingIgnoreCase(String name) {
        return unitOfWork.getCategoryRepository().findByNameContainingIgnoreCase(name);
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<Topic>> findAllTopicsByNameContainingIgnoreCase(String name) {
        return unitOfWork.getTopicRepository().findByNameContainingIgnoreCase(name);
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<Post>> findAllPostsByTitleContainingIgnoreCase(String title) {
        return unitOfWork.getPostRepository().findByTitleContainingIgnoreCase(title);
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<Post>> findAllPostsByContentContainingIgnoreCase(String content) {
        return unitOfWork.getPostRepository().findByContentContainingIgnoreCase(content);
    }

    private Account getCurrentUser() {
        var context = SecurityContextHolder.getContext();
        return unitOfWork.getAccountRepository().findByUsername(context.getAuthentication().getName()).orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    @Async("AsyncTaskExecutor")
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<FilterTransactionResponse> filter(boolean viewTransaction, boolean dailyPoint, boolean bonusPoint, Date startDate, Date endDate) {
        Account currentUser = getCurrentUser();

        validateDates(startDate, endDate);

        if (startDate != null && endDate == null) {
            endDate = startDate;
        }

        boolean includeAll = !viewTransaction && !dailyPoint && !bonusPoint;

        CompletableFuture<List<TransactionResponse>> transactionFuture = getTransactionFuture(viewTransaction, includeAll, currentUser, startDate, endDate);
        CompletableFuture<List<DailyPoint>> dailyPointFuture = getDailyPointFuture(dailyPoint, includeAll, currentUser, startDate, endDate);
        CompletableFuture<List<DailyPoint>> bonusPointFuture = getBonusPointFuture(bonusPoint, includeAll, currentUser, startDate, endDate);

        return CompletableFuture.allOf(transactionFuture, dailyPointFuture, bonusPointFuture)
                .thenApply(listFinal -> {
                    FilterTransactionResponse response = new FilterTransactionResponse();
                    response.setTransactionList(transactionFuture.join());
                    response.setDailyPointList(dailyPointFuture.join());
                    response.setBonusPoint(bonusPointFuture.join());
                    return response;
                });
    }

    private void validateDates(Date startDate, Date endDate) {
        if (startDate != null && startDate.after(new Date())) {
            throw new AppException(ErrorCode.START_DATE_AFTER_TODAY);
        }
        if (endDate != null && startDate != null && endDate.before(startDate)) {
            throw new AppException(ErrorCode.END_DATE_BEFORE_START_DATE);
        }
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<TransactionResponse>> getTransactionFuture(boolean viewTransaction, boolean includeAll, Account currentUser, Date startDate, Date endDate) {
        if (viewTransaction || includeAll) {
            if (startDate != null && endDate != null) {
                return unitOfWork.getTransactionRepository()
                        .findByWallet_AccountAndCreatedDateBetweenOrderByCreatedDateDesc(currentUser, startDate, endDate)
                        .thenApply(transactions -> transactions.stream()
                                .map(transactionMapper::toTransactionResponse)
                                .collect(Collectors.toList()));
            } else {
                return unitOfWork.getTransactionRepository()
                        .findByWallet_AccountOrderByCreatedDateDesc(currentUser)
                        .thenApply(transactions -> transactions.stream()
                                .map(transactionMapper::toTransactionResponse)
                                .collect(Collectors.toList()));
            }
        }
        return CompletableFuture.completedFuture(Collections.emptyList());
    }
    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<DailyPoint>> getDailyPointFuture(boolean dailyPoint, boolean includeAll, Account currentUser, Date startDate, Date endDate) {
        if (dailyPoint || includeAll) {
            if (startDate != null && endDate != null) {
                return unitOfWork.getDailyPointRepository()
                        .findByAccountAndTypeBonusIsNullAndCreatedDateBetweenOrderByCreatedDateDesc(currentUser, startDate, endDate);
            } else {
                return unitOfWork.getDailyPointRepository()
                        .findByAccountAndTypeBonusIsNullOrderByCreatedDateDesc(currentUser);
            }
        }
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<DailyPoint>> getBonusPointFuture(boolean bonusPoint, boolean includeAll, Account currentUser, Date startDate, Date endDate) {
        if (bonusPoint || includeAll) {
            if (startDate != null && endDate != null) {
                return unitOfWork.getDailyPointRepository()
                        .findByAccountAndTypeBonusIsNotNullAndCreatedDateBetweenOrderByCreatedDateDesc(currentUser, startDate, endDate);
            } else {
                return unitOfWork.getDailyPointRepository()
                        .findByAccountAndTypeBonusIsNotNullOrderByCreatedDateDesc(currentUser);
            }
        }
        return CompletableFuture.completedFuture(Collections.emptyList());
    }
    
}
