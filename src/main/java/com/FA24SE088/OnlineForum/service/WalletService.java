package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.Wallet2Request;
import com.FA24SE088.OnlineForum.dto.request.WalletRequest;
import com.FA24SE088.OnlineForum.dto.response.WalletResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Wallet;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.repository.AccountRepository;
import com.FA24SE088.OnlineForum.repository.WalletRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class WalletService {
    AccountRepository accountRepository;
    WalletRepository walletRepository;

    public Wallet create(WalletRequest request) {
        Account account = accountRepository.findById(request.getAccountID()).orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        if (account.getWallet() != null) throw new AppException(ErrorCode.WALLET_IS_EXISTED);
        Wallet wallet = Wallet.builder()
                .account(account)
                .balance(0)
                .build();
        return walletRepository.save(wallet);
    }

    public WalletResponse update(Wallet2Request request) {
        Account account = accountRepository.findById(request.getAccountId()).orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        Wallet response = account.getWallet();
        if (response == null) throw new AppException(ErrorCode.WALLET_NOT_EXIST);
        response.setBalance(request.getBalance());

        walletRepository.save(response);
        return WalletResponse.builder()
                .walletId(response.getWalletId())
                .balance(response.getBalance())
                .build();
    }


    public WalletResponse getWalletByAccountID(UUID id) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        Wallet wallet = account.getWallet();
        if (wallet == null) throw new AppException(ErrorCode.WALLET_NOT_EXIST);
        return WalletResponse.builder()
                .walletId(wallet.getWalletId())
                .balance(wallet.getBalance())
                .build();
    }

    public void delete(UUID id) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        account.setWallet(null);
        accountRepository.save(account);
    }
}
