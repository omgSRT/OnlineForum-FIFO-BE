package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.RedeemRequest;
import com.FA24SE088.OnlineForum.dto.response.RedeemDocumentResponse;
import com.FA24SE088.OnlineForum.dto.response.RedeemResponse;
import com.FA24SE088.OnlineForum.entity.*;
import com.FA24SE088.OnlineForum.enums.TransactionType;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.RedeemMapper;
import com.FA24SE088.OnlineForum.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class RedeemService {
    RedeemRepository redeemRepository;
    AccountRepository accountRepository;
    RewardRepository rewardRepository;
    WalletRepository walletRepository;
    TransactionRepository transactionRepository;
    RedeemMapper redeemMapper;

    private Account findAccountById(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    private Reward findDocument(UUID id) {
        return rewardRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.REWARD_NOT_FOUND));
    }

    public RedeemResponse create(RedeemRequest request) {
        Account account = findAccountById(request.getAccountId());
        if (account.getRole().getName().equals("STAFF") || account.getRole().getName().equals("ADMIN")) {
            throw new AppException(ErrorCode.STAFF_AND_ADMIN_CANNOT_REDEEM);
        }
        Reward reward = findDocument(request.getRewardId());
        account.getRedeemList().forEach(redeem -> {
            if (redeem.getReward().getRewardId().equals(reward.getRewardId()))
                throw new AppException(ErrorCode.REWARD_HAS_BEEN_TAKEN);
        });

        double result = account.getWallet().getBalance() - reward.getPrice();
        if (result >= 0) {
            Wallet wallet = account.getWallet();
            wallet.setBalance(result);
            walletRepository.save(wallet);

            var amount = reward.getPrice();
            amount = -amount;

            Transaction transaction = Transaction.builder()
                    .amount(amount)
                    .createdDate(new Date())
                    .wallet(wallet)
                    .transactionType(TransactionType.REDEEM_REWARD.name())
                    .reward(reward)
                    .build();
            transactionRepository.save(transaction);
        } else {
            throw new AppException(ErrorCode.YOU_DO_NOT_HAVE_ENOUGH_POINT);
        }
        Redeem redeem = new Redeem();
        redeem.setAccount(account);
        redeem.setReward(reward);
        redeem.setCreatedDate(new Date());
        redeemRepository.save(redeem);

        return redeemMapper.toResponse(redeem);
    }


    public RedeemDocumentResponse getMyRewarded() {
        Account account = getCurrentUser();

        List<Redeem> ofAccount = redeemRepository
                .findByAccount_AccountId(account.getAccountId());

        List<Reward> rewards = ofAccount.stream()
                .map(Redeem::getReward) // Lấy document từ mỗi redeem
                .toList();

        RedeemDocumentResponse response = new RedeemDocumentResponse();
        response.setReward(rewards);
        return response;
    }


    private Account getCurrentUser() {
        var context = SecurityContextHolder.getContext();
        return accountRepository.findByUsername(context.getAuthentication().getName()).orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    public List<Redeem> getAll() {
        Account account = getCurrentUser();
        return account.getRedeemList();
    }
}
