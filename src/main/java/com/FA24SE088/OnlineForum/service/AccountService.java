package com.FA24SE088.OnlineForum.service;


import com.FA24SE088.OnlineForum.dto.request.AccountUpdateRequest;
import com.FA24SE088.OnlineForum.dto.request.AccountRequest;
import com.FA24SE088.OnlineForum.dto.response.AccountResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Category;
import com.FA24SE088.OnlineForum.entity.Role;
import com.FA24SE088.OnlineForum.entity.Wallet;
import com.FA24SE088.OnlineForum.enums.AccountStatus;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.AccountMapper;

import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import com.FA24SE088.OnlineForum.utils.PaginationUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


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
    @Autowired
    PaginationUtils paginationUtils;

    public AccountResponse create(AccountRequest request, String autoWallet) {
        if (unitOfWork.getAccountRepository().existsByUsername(request.getUsername()))
            throw new AppException(ErrorCode.ACCOUNT_IS_EXISTED);
        if (unitOfWork.getAccountRepository().existsByEmail(request.getEmail()))
            throw new AppException(ErrorCode.EMAIL_IS_EXISTED);
        Account account = accountMapper.toAccount(request);
        account.setPassword(passwordEncoder.encode(request.getPassword()));

        if (request.getRoleName().equalsIgnoreCase("STAFF")) {
            Role role = unitOfWork.getRoleRepository().findByName(request.getRoleName());
            if (role == null) throw new AppException(ErrorCode.ROLE_NOT_FOUND);
            account.setRole(role);

            if (request.getCategoryList() != null && !request.getCategoryList().isEmpty()) {
                List<Category> categories = new ArrayList<>();
                request.getCategoryList().forEach(categoryName -> {
                    Category categoryEntity = unitOfWork.getCategoryRepository()
                            .findByName(categoryName)
                            .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

                    if (categoryEntity.getAccount() == null || categoryEntity.getAccount().getAccountId() == null) {
                        // Gán account vào category (thiết lập mối quan hệ 2 chiều)
                        categoryEntity.setAccount(account);
                        categories.add(categoryEntity);

                    } else {
                        throw new AppException(ErrorCode.CATEGORY_HAS_UNDERTAKE);
                    }
                });
                account.setCategoryList(categories); // Gán toàn bộ danh sách category vào account
            }
        } else {
            Role role = unitOfWork.getRoleRepository().findByName("USER");
            if (role == null) throw new AppException(ErrorCode.ROLE_NOT_FOUND);
            account.setRole(role);
        }
        if (autoWallet.equalsIgnoreCase("yes")) {
            Wallet wallet = new Wallet();
            wallet.setBalance(0);
            wallet.setAccount(account);
            account.setWallet(wallet);
        }
        account.setCreatedDate(new Date());
        account.setStatus(AccountStatus.PENDING_APPROVAL.name());
        AccountResponse response = accountMapper.toResponse(account);
        unitOfWork.getAccountRepository().save(account);
        return response;
    }

    public List<AccountResponse> getAll(int page, int perPage) {
        var list = unitOfWork.getAccountRepository().findAll().stream()
                .map(accountMapper::toResponse)
                .toList();
        return paginationUtils.convertListToPage(page, perPage, list);
    }

    private Account findAccount(UUID id) {
        return unitOfWork.getAccountRepository().findById(id).orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    public AccountResponse update(UUID id, AccountUpdateRequest request) {
        Account account = findAccount(id);
        if (account != null) {
            accountMapper.updateAccount(account, request);
            unitOfWork.getAccountRepository().save(account);
        }
        return accountMapper.toResponse(account);
    }

    public void delete(UUID uuid) {
        findAccount(uuid);
        unitOfWork.getAccountRepository().deleteById(uuid);
    }

    public void activeAccount(Account account) {
        account.setStatus(AccountStatus.ACTIVE.name());
    }


    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public Account findByUsername(String username) {
        return unitOfWork.getAccountRepository().findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    public Account findByEmail(String email) {
        if (unitOfWork.getAccountRepository().findByEmail(email) == null)
            throw new AppException(ErrorCode.ACCOUNT_NOT_FOUND);
        return unitOfWork.getAccountRepository().findByEmail(email);
    }
}
