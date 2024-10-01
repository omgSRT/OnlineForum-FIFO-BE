package com.FA24SE088.OnlineForum.service;


import com.FA24SE088.OnlineForum.dto.requests.AccountRequest;
import com.FA24SE088.OnlineForum.dto.response.AccountResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Role;
import com.FA24SE088.OnlineForum.enums.AccountStatus;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mappers.AccountMapper;

import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;


import org.springframework.security.access.prepost.PreAuthorize;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@Service
public class AccountService {

    @Autowired
    UnitOfWork unitOfWork;
    @Autowired
    AccountMapper accountMapper;
    @Autowired
    PasswordEncoder passwordEncoder;

    private boolean isEmailExist(String email){
        return unitOfWork.getAccountRepository().findByEmail(email) !=null;
    }

    public AccountResponse create(AccountRequest request){
        if(unitOfWork.getAccountRepository().existsByUsername(request.getUsername()))
            throw new AppException(ErrorCode.ACCOUNT_IS_EXISTED);
        if(unitOfWork.getAccountRepository().existsByEmail(request.getEmail()))
            throw new AppException(ErrorCode.EMAIL_IS_EXISTED);
        Account account = accountMapper.toAccount(request);
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        if (request.getRoleName() == null) {
            Role role = unitOfWork.getRoleRepository().findByName("USER");
            if(role == null) throw new AppException(ErrorCode.ROLE_NOT_FOUND);
            account.setRole(role);
        }
        account.setCreatedDate(new Date());
        account.setStatus(AccountStatus.PENDING_APPROVAL.name());
        AccountResponse response = accountMapper.toResponse(account);
        unitOfWork.getAccountRepository().save(account);
        return response;
    }

    public void activeUser(Account account) {
        account.setStatus(AccountStatus.ACTIVE.name());
    }


    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public Account findByUsername(String username){
        return unitOfWork.getAccountRepository().findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }
}
