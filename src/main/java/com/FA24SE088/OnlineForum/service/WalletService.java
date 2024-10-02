package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.WalletRequest;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Wallet;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@Service
public class WalletService {
    @Autowired
    UnitOfWork unitOfWork;

    public Wallet create(WalletRequest request) {
        Account account = unitOfWork.getAccountRepository().findById(request.getAccountID()).orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        if (account.getWallet() != null) throw new AppException(ErrorCode.WALLET_IS_EXISTED);
        Wallet wallet = Wallet.builder()
                .account(account)
                .balance(0)
                .build();
        return unitOfWork.getWalletRepository().save(wallet);
    }

    public void update(UUID id, double balance) {
        Wallet wallet = getByID(id);
        wallet.setBalance(balance);
        unitOfWork.getWalletRepository().save(wallet);
    }

    public Wallet getByID(UUID id){
        return unitOfWork.getWalletRepository().findById(id).orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_EXIST));
    }

}
