package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.response.*;
import com.FA24SE088.OnlineForum.entity.*;
import com.FA24SE088.OnlineForum.enums.PostStatus;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.DailyPointMapper;
import com.FA24SE088.OnlineForum.mapper.OrderPointMapper;
import com.FA24SE088.OnlineForum.mapper.TransactionMapper;
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
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    DailyPointMapper dailyPointMapper;
    OrderPointMapper orderPointMapper;
    AccountRepository accountRepository;
    CategoryRepository categoryRepository;
    TopicRepository topicRepository;
    PostRepository postRepository;
    TransactionRepository transactionRepository;
    DailyPointRepository dailyPointRepository;
    OrderPointRepository orderPointRepository;
    BlockedAccountRepository blockedAccountRepository;
    PaginationUtils paginationUtils;

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

    @Async("AsyncTaskExecutor")
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<FilterTransactionResponse> filter(boolean viewTransaction,
                                                               boolean dailyPoint,
                                                               boolean bonusPoint,
                                                               boolean orderPoint,
                                                               String startDateStr, String endDateStr) {
        Account currentUser = getCurrentUser();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Date startDate = null;
        Date endDate = null;

        try {
            if (startDateStr != null) {
                startDate = simpleDateFormat.parse(startDateStr);
                startDate = resetTimeToStartOfDay(startDate);
            }
            if (endDateStr != null) {
                endDate = simpleDateFormat.parse(endDateStr);
                endDate = resetTimeToEndOfDay(endDate);
            }
        } catch (ParseException e) {
            throw new AppException(ErrorCode.WRONG_DATE_FORMAT);
        }

        validateDates(startDate, endDate);

        if (startDate != null && endDate == null) {
            endDate = startDate;
        }

        boolean includeAll = !viewTransaction && !dailyPoint && !bonusPoint && !orderPoint;

        CompletableFuture<List<TransactionResponse>> transactionFuture = getTransactionFuture(viewTransaction, includeAll, currentUser, startDate, endDate);
        CompletableFuture<List<DailyPointForFilterTransactionResponse>> dailyPointFuture = getDailyPointFuture(dailyPoint, includeAll, currentUser, startDate, endDate);
        CompletableFuture<List<DailyPointForFilterTransactionResponse>> bonusPointFuture = getBonusPointFuture(bonusPoint, includeAll, currentUser, startDate, endDate);
        CompletableFuture<List<OrderPointResponse>> orderPointFuture = getOrderPointFuture(orderPoint, includeAll, currentUser, startDate, endDate);

        return CompletableFuture.allOf(transactionFuture, dailyPointFuture, bonusPointFuture)
                .thenApply(listFinal -> {
                    FilterTransactionResponse response = new FilterTransactionResponse();
                    response.setTransactionList(transactionFuture.join());
                    response.setDailyPointList(dailyPointFuture.join());
                    response.setBonusPoint(bonusPointFuture.join());
                    response.setOrderPointList(orderPointFuture.join());
                    return response;
                });
    }
//    public CompletableFuture<FilterTransactionResponse> filter(
//            boolean viewTransaction,
//            boolean dailyPoint,
//            boolean bonusPoint,
//            boolean orderPoint,
//            String startDateStr,
//            String endDateStr,
//            int page,
//            int perPage) {
//        Account currentUser = getCurrentUser();
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
//
//        Date startDate = null;
//        Date endDate = null;
//
//        try {
//            if (startDateStr != null) {
//                startDate = simpleDateFormat.parse(startDateStr);
//                startDate = resetTimeToStartOfDay(startDate);
//            }
//            if (endDateStr != null) {
//                endDate = simpleDateFormat.parse(endDateStr);
//                endDate = resetTimeToEndOfDay(endDate);
//            }
//        } catch (ParseException e) {
//            throw new AppException(ErrorCode.WRONG_DATE_FORMAT);
//        }
//
//        validateDates(startDate, endDate);
//
//        if (startDate != null && endDate == null) {
//            endDate = startDate;
//        }
//
//        boolean includeAll = !viewTransaction && !dailyPoint && !bonusPoint && !orderPoint;
//
//        CompletableFuture<List<TransactionResponse>> transactionFuture = getTransactionFuture(viewTransaction, includeAll, currentUser, startDate, endDate, page, perPage);
//        CompletableFuture<List<DailyPointForFilterTransactionResponse>> dailyPointFuture = getDailyPointFuture(dailyPoint, includeAll, currentUser, startDate, endDate, page, perPage);
//        CompletableFuture<List<DailyPointForFilterTransactionResponse>> bonusPointFuture = getBonusPointFuture(bonusPoint, includeAll, currentUser, startDate, endDate, page, perPage);
//        CompletableFuture<List<OrderPointResponse>> orderPointFuture = getOrderPointFuture(orderPoint, includeAll, currentUser, startDate, endDate, page, perPage);
//
//        return CompletableFuture.allOf(transactionFuture, dailyPointFuture, bonusPointFuture, orderPointFuture)
//                .thenApply(listFinal -> {
//                    FilterTransactionResponse response = new FilterTransactionResponse();
//                    response.setTransactionList(transactionFuture.join());
//                    response.setDailyPointList(dailyPointFuture.join());
//                    response.setBonusPoint(bonusPointFuture.join());
//                    response.setOrderPointList(orderPointFuture.join());
//                    return response;
//                });
//    }


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
        var currentUsername = getUsernameFromJwt();
        var blockedListFuture = getBlockedAccountListByUsername(currentUsername);
        var blockerListFuture = getBlockerAccountListByUsername(currentUsername);

        return CompletableFuture.allOf(blockedListFuture, blockerListFuture).thenCompose(v ->
                accountRepository.findByUsernameContainingIgnoreCase(username)
                        .thenApply(accountList -> {
                            var blockedList = blockedListFuture.join();
                            var blockerList = blockerListFuture.join();

                            accountList = accountList.stream()
                                    .filter(account -> !account.getUsername().equals(currentUsername))
                                    .filter(account -> account.getStatus() != null && account.getStatus().equals("ACTIVE"))
                                    .filter(account -> !blockerList.contains(account)
                                            && !blockedList.contains(account))
                                    .toList();

                            return accountList;
                        })
        );
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<Account>> findAllAccountsByEmailContainingIgnoreCase(String email) {
        var currentUsername = getUsernameFromJwt();
        var blockedListFuture = getBlockedAccountListByUsername(currentUsername);
        var blockerListFuture = getBlockerAccountListByUsername(currentUsername);

        return CompletableFuture.allOf(blockedListFuture, blockerListFuture).thenCompose(v ->
                accountRepository.findByEmailContainingIgnoreCase(email)
                        .thenApply(accountList -> {
                            var blockedList = blockedListFuture.join();
                            var blockerList = blockerListFuture.join();

                            accountList = accountList.stream()
                                    .filter(account -> !account.getUsername().equals(currentUsername))
                                    .filter(account -> account.getStatus() != null && account.getStatus().equals("ACTIVE"))
                                    .filter(account -> !blockerList.contains(account)
                                            && !blockedList.contains(account))
                                    .toList();

                            return accountList;
                        })
        );
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<Category>> findAllCategoriesByNameContainingIgnoreCase(String name) {
        return categoryRepository.findByNameContainingIgnoreCase(name);
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<Topic>> findAllTopicsByNameContainingIgnoreCase(String name) {
        return topicRepository.findByNameContainingIgnoreCase(name);
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<Post>> findAllPostsByTitleContainingIgnoreCase(String title) {
        var username = getUsernameFromJwt();
        var blockedListFuture = getBlockedAccountListByUsername(username);
        var blockerListFuture = getBlockerAccountListByUsername(username);

        return CompletableFuture.allOf(blockedListFuture, blockerListFuture).thenCompose(v ->
                postRepository.findByTitleContainingIgnoreCaseOrderByCreatedDateDesc(title)
                        .thenApply(postList -> {
                            var blockedList = blockedListFuture.join();
                            var blockerList = blockerListFuture.join();

                            postList = postList.stream()
                                    .filter(post -> !post.getAccount().getUsername().equals(username))
                                    .filter(post -> post.getAccount().getStatus() != null
                                            && post.getAccount().getStatus().equals("ACTIVE"))
                                    .filter(post -> post.getStatus().equalsIgnoreCase(PostStatus.PUBLIC.name()))
                                    .filter(post -> !blockerList.contains(post.getAccount())
                                            && !blockedList.contains(post.getAccount()))
                                    .toList();

                            return postList;
                        })
        );
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<Post>> findAllPostsByContentContainingIgnoreCase(String content) {
        var username = getUsernameFromJwt();
        var blockedListFuture = getBlockedAccountListByUsername(username);
        var blockerListFuture = getBlockerAccountListByUsername(username);

        return CompletableFuture.allOf(blockedListFuture, blockerListFuture).thenCompose(v ->
                postRepository.findByContentContainingIgnoreCaseOrderByCreatedDateDesc(content)
                        .thenApply(postList -> {
                            var blockedList = blockedListFuture.join();
                            var blockerList = blockerListFuture.join();

                            postList = postList.stream()
                                    .filter(post -> !post.getAccount().getUsername().equals(username))
                                    .filter(post -> post.getAccount().getStatus() != null
                                            && post.getAccount().getStatus().equals("ACTIVE"))
                                    .filter(post -> post.getStatus().equalsIgnoreCase(PostStatus.PUBLIC.name()))
                                    .filter(post -> !blockerList.contains(post.getAccount())
                                            && !blockedList.contains(post.getAccount()))
                                    .toList();

                            return postList;
                        })
        );
    }

    private Account getCurrentUser() {
        var context = SecurityContextHolder.getContext();
        return accountRepository.findByUsername(context.getAuthentication().getName()).orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    private void validateDates(Date startDate, Date endDate) {
        if (startDate != null && startDate.after(new Date())) {
            throw new AppException(ErrorCode.START_DATE_AFTER_TODAY);
        }
        if (endDate != null && startDate != null && endDate.before(startDate)) {
            throw new AppException(ErrorCode.END_DATE_BEFORE_START_DATE);
        }
        if (startDate == null && endDate != null) {
            throw new AppException(ErrorCode.START_DATE_CANNOT_NULL);
        }
    }

    private Date resetTimeToStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Date resetTimeToEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

        @Async("AsyncTaskExecutor")
    private CompletableFuture<List<TransactionResponse>> getTransactionFuture(boolean viewTransaction, boolean includeAll, Account currentUser, Date startDate, Date endDate) {
        if (viewTransaction || includeAll) {
            if (startDate != null && endDate != null) {
                return transactionRepository
                        .findByWallet_AccountAndCreatedDateBetweenOrderByCreatedDateDesc(currentUser, startDate, endDate)
                        .thenApply(transactions -> transactions.stream()
                                .map(transactionMapper::toTransactionResponse)
                                .collect(Collectors.toList()));
            } else {
                return transactionRepository
                        .findByWallet_AccountOrderByCreatedDateDesc(currentUser)
                        .thenApply(transactions -> transactions.stream()
                                .map(transactionMapper::toTransactionResponse)
                                .collect(Collectors.toList()));
            }
        }
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<DailyPointForFilterTransactionResponse>> getDailyPointFuture(boolean dailyPoint, boolean includeAll, Account currentUser, Date startDate, Date endDate) {
        if (dailyPoint || includeAll) {
            if (startDate != null && endDate != null) {
                return dailyPointRepository
                        .findByAccountAndTypeBonusIsNullAndCreatedDateBetweenOrderByCreatedDateDesc(currentUser, startDate, endDate)
                        .thenApply(dailyPointMapper::toListResponse);
            } else {
                return dailyPointRepository
                        .findByAccountAndTypeBonusIsNullOrderByCreatedDateDesc(currentUser)
                        .thenApply(dailyPointMapper::toListResponse);
            }
        }
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<DailyPointForFilterTransactionResponse>> getBonusPointFuture(boolean bonusPoint, boolean includeAll, Account currentUser, Date startDate, Date endDate) {
        if (bonusPoint || includeAll) {
            if (startDate != null && endDate != null) {
                return dailyPointRepository
                        .findByAccountAndTypeBonusIsNotNullAndCreatedDateBetweenOrderByCreatedDateDesc(currentUser, startDate, endDate)
                        .thenApply(dailyPointMapper::toListResponse);
            } else {
                return dailyPointRepository
                        .findByAccountAndTypeBonusIsNotNullOrderByCreatedDateDesc(currentUser)
                        .thenApply(dailyPointMapper::toListResponse);
            }
        }
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<OrderPointResponse>> getOrderPointFuture(boolean orderPoint, boolean includeAll, Account currentUser, Date startDate, Date endDate) {
        if (orderPoint || includeAll) {
            if (startDate != null && endDate != null) {
                return orderPointRepository
                        .findByWallet_AccountAndOrderDateBetweenOrderByOrderDateDesc(currentUser, startDate, endDate)
                        .thenApply(orderPointMapper::toOderPointResponseList);
            } else {
                return orderPointRepository
                        .findByWallet_AccountOrderByOrderDateDesc(currentUser)
                        .thenApply(orderPointMapper::toOderPointResponseList);
            }
        }
        return CompletableFuture.completedFuture(Collections.emptyList());
    }


    private String getUsernameFromJwt() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaim("username");
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
    private CompletableFuture<List<Account>> getBlockedAccountListByUsername(String username) {
        var accountFuture = findAccountByUsername(username);

        return accountFuture.thenApply(account -> {
            var blockedAccountEntityList = blockedAccountRepository.findByBlocker(account);

            return blockedAccountEntityList.stream()
                    .map(BlockedAccount::getBlocked)
                    .toList();
        });
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<Account>> getBlockerAccountListByUsername(String username) {
        var accountFuture = findAccountByUsername(username);

        return accountFuture.thenApply(account -> {
            var blockedAccountEntityList = blockedAccountRepository.findByBlocked(account);

            return blockedAccountEntityList.stream()
                    .map(BlockedAccount::getBlocker)
                    .toList();
        });
    }
}
