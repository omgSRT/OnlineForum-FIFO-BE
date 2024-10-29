package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.RedeemRequest;
import com.FA24SE088.OnlineForum.dto.response.RedeemDocumentResponse;
import com.FA24SE088.OnlineForum.dto.response.RedeemResponse;
import com.FA24SE088.OnlineForum.entity.*;
import com.FA24SE088.OnlineForum.enums.TransactionType;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.RedeemMapper;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
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
    UnitOfWork unitOfWork;
    RedeemMapper redeemMapper;

    private Account findAcc(UUID id){
        return unitOfWork.getAccountRepository().findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }
    private Reward findDocument(UUID id){
        return unitOfWork.getRewardRepository().findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.REWARD_NOT_FOUND));
    }
    public RedeemResponse create_2(RedeemRequest request){
        Account account = findAcc(request.getAccountId());
        Reward reward = findDocument(request.getRewardId());
        //nếu tk đã đổi phần thưởng này r thì ko cho đổi nữa
        account.getRedeemList().forEach(redeem -> {
            if (redeem.getReward().getDocumentId().equals(reward.getDocumentId()))
                throw new AppException(ErrorCode.REWARD_HAS_BEEN_TAKEN);
        });

        double result = account.getWallet().getBalance() - reward.getPrice();
        if(result >= 0){
            Wallet wallet = account.getWallet();
            wallet.setBalance(result);
            unitOfWork.getWalletRepository().save(wallet);

            Transaction transaction = Transaction.builder()
                    .amount(reward.getPrice())
                    .type(TransactionType.DEBIT.name())
                    .createdDate(new Date())
                    .wallet(wallet)
                    .build();
            unitOfWork.getTransactionRepository().save(transaction);
        }else {
            throw new AppException(ErrorCode.YOU_DO_NOT_HAVE_ENOUGH_POINT);
        }
        Redeem redeem = new Redeem();
        redeem.setAccount(account);
        redeem.setReward(reward);
        redeem.setCreatedDate(new Date());
        unitOfWork.getRedeemRepository().save(redeem);

        return redeemMapper.toResponse(redeem);
    }



    public RedeemDocumentResponse getDocumentRewarded() {
        Account account = getCurrentUser();

        List<Redeem> ofAccount = unitOfWork.getRedeemRepository()
                .findByAccount_AccountId(account.getAccountId());

        List<Reward> rewards = ofAccount.stream()
                .map(Redeem::getReward) // Lấy document từ mỗi redeem
                .toList();

        RedeemDocumentResponse response = new RedeemDocumentResponse();
        response.setReward(rewards);
        return response;
    }



    private Account getCurrentUser(){
        var context = SecurityContextHolder.getContext();
        return unitOfWork.getAccountRepository().findByUsername(context.getAuthentication().getName()).orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }
    public List<Redeem> getAll(){
        Account account = getCurrentUser();
        return account.getRedeemList();
    }
}
