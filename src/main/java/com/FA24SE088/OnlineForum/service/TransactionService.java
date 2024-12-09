package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.TransactionRequest;
import com.FA24SE088.OnlineForum.dto.response.TransactionResponse;
import com.FA24SE088.OnlineForum.entity.*;
import com.FA24SE088.OnlineForum.enums.TransactionType;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.TransactionMapper;
import com.FA24SE088.OnlineForum.repository.AccountRepository;
import com.FA24SE088.OnlineForum.repository.RewardRepository;
import com.FA24SE088.OnlineForum.repository.TransactionRepository;
import com.FA24SE088.OnlineForum.repository.WalletRepository;
import com.FA24SE088.OnlineForum.utils.PaginationUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class TransactionService {
    TransactionRepository transactionRepository;
    AccountRepository accountRepository;
    RewardRepository rewardRepository;
    WalletRepository walletRepository;
    PaginationUtils paginationUtils;
    TransactionMapper transactionMapper;

    @Async("AsyncTaskExecutor")
    public CompletableFuture<TransactionResponse> createTransaction(TransactionRequest request) {
        var accountFuture = findAccountById(request.getAccountId());

        return CompletableFuture.allOf(accountFuture).thenCompose(v -> {
                    var account = accountFuture.join();

                    if (request.getTransactionType().equals(TransactionType.REDEEM_REWARD)) {
                        return findRewardById(request.getRewardId()).thenCompose(reward -> {
                            var checkFuture = checkTransactionExistByWalletAndReward(account.getWallet(), reward);
                            return checkFuture.thenCompose(check -> {
                                if (check) {
                                    throw new AppException(ErrorCode.REWARD_HAS_BEEN_TAKEN);
                                }
                                return CompletableFuture.completedFuture(createAndSaveTransaction(request, account, reward));
                            });
                        });
                    }

                    return CompletableFuture.completedFuture(createAndSaveTransaction(request, account, null));
                })
                .thenApply(transactionMapper::toTransactionResponse);
    }

    private Account getCurrentUser() {
        var context = SecurityContextHolder.getContext();
        return accountRepository.findByUsername(context.getAuthentication().getName()).orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    public List<TransactionResponse> getListByAccountId() {
        Account account = getCurrentUser();
        return transactionRepository.findByWallet(account.getWallet()).stream()
                .map(transactionMapper::toTransactionResponse).toList();
    }

    @Async("AsyncTaskExecutor")
    public CompletableFuture<List<TransactionResponse>> getAllTransaction(int page, int perPage,
                                                                          UUID accountId,
                                                                          UUID rewardId,
                                                                          TransactionType transactionType,
                                                                          String givenDate) {
        var accountFuture = accountId != null
                ? findAccountById(accountId)
                : CompletableFuture.completedFuture(null);
        var rewardFuture = rewardId != null
                ? findRewardById(rewardId)
                : CompletableFuture.completedFuture(null);

        return CompletableFuture.allOf(accountFuture, rewardFuture).thenCompose(v -> {
            var account = accountFuture.join();
            var reward = rewardFuture.join();

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date parseDate;
            try {
                parseDate = givenDate == null
                        ? null
                        : simpleDateFormat.parse(givenDate);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            Date finalParseDate = parseDate;

            var transactionListFuture = transactionRepository.findAllByOrderByCreatedDateDesc();

            return transactionListFuture.thenCompose(list -> {
                var filterList = list.stream()
                        .filter(transaction -> account == null || transaction.getWallet().getAccount().equals(account))
                        .filter(transaction -> reward == null || (reward != null && transaction.getReward().equals(reward)))
                        .filter(transaction -> transactionType == null ||
                                transaction.getTransactionType().equals(transactionType.name()))
                        .filter(transaction -> {
                            Calendar createdDateCal = Calendar.getInstance();
                            createdDateCal.setTime(transaction.getCreatedDate());
                            createdDateCal.set(Calendar.HOUR_OF_DAY, 0);
                            createdDateCal.set(Calendar.MINUTE, 0);
                            createdDateCal.set(Calendar.SECOND, 0);
                            createdDateCal.set(Calendar.MILLISECOND, 0);

                            if (finalParseDate == null) {
                                return true;
                            }

                            Calendar parsedDateCal = Calendar.getInstance();
                            parsedDateCal.setTime(finalParseDate);
                            parsedDateCal.set(Calendar.HOUR_OF_DAY, 0);
                            parsedDateCal.set(Calendar.MINUTE, 0);
                            parsedDateCal.set(Calendar.SECOND, 0);
                            parsedDateCal.set(Calendar.MILLISECOND, 0);

                            return createdDateCal.getTime().equals(parsedDateCal.getTime());
                        })
                        .map(transactionMapper::toTransactionResponse)
                        .toList();

                return CompletableFuture.completedFuture(paginationUtils.convertListToPage(page, perPage, filterList));
            });
        });
    }

    @Async("AsyncTaskExecutor")
    public CompletableFuture<List<TransactionResponse>> getAllTransactionForCurrentUser(int page, int perPage,
                                                                                        String givenDate,
                                                                                        boolean isListAscendingByCreatedDate) {
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);

        return accountFuture.thenCompose(account -> {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date parseDate;
            try {
                parseDate = givenDate == null
                        ? null
                        : simpleDateFormat.parse(givenDate);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            Date finalParseDate = parseDate;

            var transactionListFuture = findTransactionListByIsListAscendingByCreatedDate(isListAscendingByCreatedDate);

            return transactionListFuture.thenCompose(list -> {
                var filterList = list.stream()
                        .filter(transaction -> transaction.getWallet().getAccount().equals(account))
                        .filter(transaction -> {
                            Calendar createdDateCal = Calendar.getInstance();
                            createdDateCal.setTime(transaction.getCreatedDate());
                            createdDateCal.set(Calendar.HOUR_OF_DAY, 0);
                            createdDateCal.set(Calendar.MINUTE, 0);
                            createdDateCal.set(Calendar.SECOND, 0);
                            createdDateCal.set(Calendar.MILLISECOND, 0);

                            if (finalParseDate == null) {
                                return true;
                            }

                            Calendar parsedDateCal = Calendar.getInstance();
                            parsedDateCal.setTime(finalParseDate);
                            parsedDateCal.set(Calendar.HOUR_OF_DAY, 0);
                            parsedDateCal.set(Calendar.MINUTE, 0);
                            parsedDateCal.set(Calendar.SECOND, 0);
                            parsedDateCal.set(Calendar.MILLISECOND, 0);

                            return createdDateCal.getTime().equals(parsedDateCal.getTime());
                        })
                        .map(transactionMapper::toTransactionResponse)
                        .toList();

                return CompletableFuture.completedFuture(paginationUtils.convertListToPage(page, perPage, filterList));
            });
        });
    }

    @Async("AsyncTaskExecutor")
    public CompletableFuture<TransactionResponse> getTransactionById(UUID transactionId) {
        var transactionFuture = findTransactionById(transactionId);
        return transactionFuture.thenApply(transactionMapper::toTransactionResponse);
    }

    @Async("AsyncTaskExecutor")
    public CompletableFuture<TransactionResponse> deleteTransaction(UUID transactionId) {
        var transactionFuture = findTransactionById(transactionId);

        return transactionFuture.thenCompose(transaction -> {
            transactionRepository.delete(transaction);

            return CompletableFuture.completedFuture(transactionMapper.toTransactionResponse(transaction));
        });
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<Account> findAccountById(UUID accountId) {
        return CompletableFuture.supplyAsync(() ->
                accountRepository.findById(accountId)
                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND))
        );
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<Reward> findRewardById(UUID rewardId) {
        return CompletableFuture.supplyAsync(() ->
                rewardRepository.findById(rewardId)
                        .orElseThrow(() -> new AppException(ErrorCode.REWARD_NOT_FOUND))
        );
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<Boolean> checkTransactionExistByWalletAndReward(Wallet wallet, Reward reward) {
        return transactionRepository.existsByWalletAndReward(wallet, reward);
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<Transaction> findTransactionById(UUID transactionId) {
        return CompletableFuture.supplyAsync(() ->
                transactionRepository.findById(transactionId)
                        .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND))
        );
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<Transaction>> findTransactionListByIsListAscendingByCreatedDate(boolean isListAscendingByCreatedDate) {
        if (!isListAscendingByCreatedDate) {
            return transactionRepository.findAllByOrderByCreatedDateDesc();
        } else {
            return transactionRepository.findAllByOrderByCreatedDateAsc();
        }
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<Account> findAccountByUsername(String username) {
        return CompletableFuture.supplyAsync(() ->
                accountRepository.findByUsername(username)
                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND))
        );
    }

    private String getUsernameFromJwt() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaim("username");
        }
        return null;
    }

    private Transaction createAndSaveTransaction(TransactionRequest request, Account account, Reward reward) {
        Transaction newTransaction = transactionMapper.toTransaction(request);
        var amount = -request.getAmount();
        newTransaction.setAmount(amount);
        newTransaction.setCreatedDate(new Date());
        newTransaction.setWallet(account.getWallet());
        newTransaction.setReward(reward);
        newTransaction.setTransactionType(request.getTransactionType().name());

        walletRepository.save(account.getWallet());

        return transactionRepository.save(newTransaction);
    }

    private TransactionType safeValueOf(String type) {
        try {
            return TransactionType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
