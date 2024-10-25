package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.TransactionRequest;
import com.FA24SE088.OnlineForum.dto.response.TransactionResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Transaction;
import com.FA24SE088.OnlineForum.entity.Wallet;
import com.FA24SE088.OnlineForum.enums.TransactionType;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.TransactionMapper;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import com.FA24SE088.OnlineForum.utils.PaginationUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class TransactionService {
    UnitOfWork unitOfWork;
    PaginationUtils paginationUtils;
    TransactionMapper transactionMapper;

    @Async("AsyncTaskExecutor")
    public CompletableFuture<TransactionResponse> createTransaction(TransactionRequest request, TransactionType type){
        var accountFuture = findAccountById(request.getAccountId());

        return accountFuture.thenCompose(account -> {
            Transaction newTransaction = transactionMapper.toTransaction(request);
            newTransaction.setCreatedDate(new Date());
            newTransaction.setType(type.name());
            newTransaction.setWallet(account.getWallet());

            var wallet = account.getWallet();
            var balance = wallet.getBalance();
            if(type.name().equals(TransactionType.CREDIT.name())){
                wallet.setBalance(balance + request.getAmount());
            }
            if(type.name().equals(TransactionType.DEBIT.name())){
                wallet.setBalance(balance - request.getAmount());
            }

            unitOfWork.getWalletRepository().save(wallet);

            return CompletableFuture.completedFuture(unitOfWork.getTransactionRepository().save(newTransaction));
        })
                .thenApply(transactionMapper::toTransactionResponse);
    }
    @Async("AsyncTaskExecutor")
    public CompletableFuture<List<TransactionResponse>> getAllTransaction(int page, int perPage, UUID accountId, String givenDate){
        var accountFuture = accountId != null
                ? findAccountById(accountId)
                : CompletableFuture.completedFuture(null);

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

                    var list = unitOfWork.getTransactionRepository().findAllByOrderByCreatedDateDesc().stream()
                            .filter(transaction -> account == null || transaction.getWallet().getAccount().equals(account))
                            .filter(transaction -> {
                                Calendar createdDateCal = Calendar.getInstance();
                                createdDateCal.setTime(transaction.getCreatedDate());
                                createdDateCal.set(Calendar.HOUR_OF_DAY, 0);
                                createdDateCal.set(Calendar.MINUTE, 0);
                                createdDateCal.set(Calendar.SECOND, 0);
                                createdDateCal.set(Calendar.MILLISECOND, 0);

                                if(finalParseDate == null){
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

                    return CompletableFuture.completedFuture(list);
                });
    }
    @Async("AsyncTaskExecutor")
    public CompletableFuture<TransactionResponse> getTransactionById(UUID transactionId){
        var transactionFuture = findTransactionById(transactionId);
        return transactionFuture.thenApply(transactionMapper::toTransactionResponse);
    }
    @Async("AsyncTaskExecutor")
    public CompletableFuture<TransactionResponse> deleteTransaction(UUID transactionId){
        var transactionFuture = findTransactionById(transactionId);

        return transactionFuture.thenCompose(transaction -> {
            var amount = transaction.getAmount();
            var wallet = transaction.getWallet();
            var balance = wallet.getBalance();

            if (transaction.getType().equals(TransactionType.CREDIT.name())) {
                wallet.setBalance(balance - amount);
            } else if (transaction.getType().equals(TransactionType.DEBIT.name())) {
                wallet.setBalance(balance + amount);
            }

            unitOfWork.getWalletRepository().save(wallet);
            unitOfWork.getTransactionRepository().delete(transaction);

            return CompletableFuture.completedFuture(transactionMapper.toTransactionResponse(transaction));
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
    private CompletableFuture<Transaction> findTransactionById(UUID transactionId) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getTransactionRepository().findById(transactionId)
                        .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND))
        );
    }
}
