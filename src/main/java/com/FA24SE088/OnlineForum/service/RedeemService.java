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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private Document findSource(UUID id){
        return unitOfWork.getDocumentRepository().findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));
    }
//    public Redeem create(RedeemRequest request){
//        Account account = findAcc(request.getAccountId());
//        Document document = findSource(request.getSourceCodeId());
//        //nếu tk đã đổi phần thưởng này r thì ko cho đổi nữa
//        account.getRedeemList().forEach(redeem -> {
//            if (redeem.getDocument().getDocumentId().equals(document.getDocumentId()))
//                throw new AppException(ErrorCode.REWARD_HAS_BEEN_TAKEN);
//        });
//
//        double result = account.getWallet().getBalance() - document.getPrice();
//        if(result >= 0){
//            Wallet wallet = account.getWallet();
//            wallet.setBalance(result);
//            unitOfWork.getWalletRepository().save(wallet);
//
//            Transaction transaction = Transaction.builder()
//                    .amount(document.getPrice())
//                    .type(TransactionType.DEBIT.name())
//                    .createdDate(new Date())
//                    .wallet(wallet)
//                    .build();
//            unitOfWork.getTransactionRepository().save(transaction);
//        }else {
//            throw new AppException(ErrorCode.YOU_DO_NOT_HAVE_ENOUGH_POINT);
//        }
//        Redeem redeem = new Redeem();
//        redeem.setAccount(account);
//        redeem.setDocument(document);
//        redeem.setCreatedDate(new Date());
//        unitOfWork.getRedeemRepository().save(redeem);
//        return redeem;
//    }

    public RedeemResponse create_2(RedeemRequest request){
        Account account = findAcc(request.getAccountId());
        Document document = findSource(request.getSourceCodeId());
        //nếu tk đã đổi phần thưởng này r thì ko cho đổi nữa
        account.getRedeemList().forEach(redeem -> {
            if (redeem.getDocument().getDocumentId().equals(document.getDocumentId()))
                throw new AppException(ErrorCode.REWARD_HAS_BEEN_TAKEN);
        });

        double result = account.getWallet().getBalance() - document.getPrice();
        if(result >= 0){
            Wallet wallet = account.getWallet();
            wallet.setBalance(result);
            unitOfWork.getWalletRepository().save(wallet);

            Transaction transaction = Transaction.builder()
                    .amount(document.getPrice())
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
        redeem.setDocument(document);
        redeem.setCreatedDate(new Date());
        unitOfWork.getRedeemRepository().save(redeem);

        return redeemMapper.toResponse(redeem);
    }



    public RedeemDocumentResponse getDocumentRewarded() {
        Account account = getCurrentUser();

        List<Redeem> ofAccount = unitOfWork.getRedeemRepository()
                .findByAccount_AccountId(account.getAccountId());

        List<Document> documents = ofAccount.stream()
                .map(Redeem::getDocument) // Lấy document từ mỗi redeem
                .toList();

        RedeemDocumentResponse response = new RedeemDocumentResponse();
        response.setDocument(documents);
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
