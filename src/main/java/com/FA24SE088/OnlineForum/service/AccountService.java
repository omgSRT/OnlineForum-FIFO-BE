package com.FA24SE088.OnlineForum.service;


import com.FA24SE088.OnlineForum.dto.request.AccountUpdateCategoryRequest;
import com.FA24SE088.OnlineForum.dto.request.AccountUpdateInfoRequest;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


import org.springframework.security.access.prepost.PreAuthorize;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class AccountService {
    UnitOfWork unitOfWork;
    AccountMapper accountMapper;
    PasswordEncoder passwordEncoder;
    PaginationUtils paginationUtils;

    public AccountResponse create(AccountRequest request) {
        if (unitOfWork.getAccountRepository().existsByUsername(request.getUsername()))
            throw new AppException(ErrorCode.ACCOUNT_IS_EXISTED);
        if (unitOfWork.getAccountRepository().existsByEmail(request.getEmail()))
            throw new AppException(ErrorCode.EMAIL_IS_EXISTED);
        if(!request.getPassword().equals(request.getConfirmPassword())){
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }
        Account account = accountMapper.toAccount(request);
        account.setHandle(request.getUsername());
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
                account.setCategoryList(categories);
            }
        } else {
            Role role = unitOfWork.getRoleRepository().findByName("USER");
            if (role == null) throw new AppException(ErrorCode.ROLE_NOT_FOUND);
            account.setRole(role);
        }

        Wallet wallet = new Wallet();
        wallet.setBalance(0);
        wallet.setAccount(account);
        account.setWallet(wallet);

        account.setCreatedDate(new Date());
        account.setStatus(AccountStatus.PENDING_APPROVAL.name());
        String handle = String.format("@%s", request.getUsername());
        account.setHandle(handle);
        AccountResponse response = accountMapper.toResponse(account);
        response.setAccountId(account.getAccountId());
        unitOfWork.getAccountRepository().save(account);
        return response;
    }

    public AccountResponse updateCategoryOfStaff(UUID id, AccountUpdateCategoryRequest request) {
        Account account = findAccount(id);

        if (account.getRole().getName().equals("STAFF")) {
            List<Category> currentCategoryList = account.getCategoryList();
            List<Category> newCategoryList = new ArrayList<>();
            request.getCategoryList().forEach(cateName -> {
                Category category = unitOfWork.getCategoryRepository().findByName(cateName)
                        .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

                if (category.getAccount() == null || category.getAccount().getAccountId().equals(account.getAccountId())) {
                    category.setAccount(account);
                    newCategoryList.add(category);
                } else {
                    throw new AppException(ErrorCode.CATEGORY_HAS_UNDERTAKE);
                }
            });

            // Cập nhật các category không còn thuộc staff -> set account về null
            for (Category oldCategory : currentCategoryList) {
                if (!newCategoryList.contains(oldCategory)) {
                    oldCategory.setAccount(null);
                    unitOfWork.getCategoryRepository().save(oldCategory);
                }
            }
            // Xóa các category cũ trong danh sách hiện tại
            currentCategoryList.clear();

            // Thêm tất cả category mới vào danh sách hiện tại
            currentCategoryList.addAll(newCategoryList);

            unitOfWork.getAccountRepository().save(account);
        }

        return accountMapper.toResponse(account);
    }

    public AccountResponse updateInfo(AccountUpdateInfoRequest request) {
        Account account = getCurrentUser();
        if (!passwordEncoder.matches(request.getOldPassword(), account.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }
        if (!request.getNewPass().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }
        // Mã hóa mật khẩu mới (encoder là mã hoá 1 chiều)
        account.setPassword(passwordEncoder.encode(request.getNewPass()));
        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            account.setAvatar(request.getAvatar());
        }
        if (request.getCoverImage() != null && !request.getCoverImage().isEmpty()) {
            account.setCoverImage(request.getCoverImage());
        }
        unitOfWork.getAccountRepository().save(account);
        return accountMapper.toResponse(account);
    }

    private Account getCurrentUser(){
        var context = SecurityContextHolder.getContext();
        return unitOfWork.getAccountRepository().findByUsername(context.getAuthentication().getName()).orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    public List<AccountResponse> getAll(int page, int perPage) {
        var list = unitOfWork.getAccountRepository().findAll().stream()
                .map(accountMapper::toResponse)
                .toList();
        return paginationUtils.convertListToPage(page, perPage, list);
    }

    public List<AccountResponse> filter(int page, int perPage, String username, String email, AccountStatus status) {
        List<AccountResponse> result = unitOfWork.getAccountRepository().findAll().stream()
                .map(accountMapper::toResponse)
                .filter(x -> (username == null || (x.getUsername() != null && x.getUsername().contains(username))))
                .filter(x -> (email == null || (x.getEmail() != null && x.getEmail().contains(email))))
                .filter(x -> (status == null || (x.getStatus() != null && x.getStatus().contains(status.name()))))
                .toList();
        return paginationUtils.convertListToPage(page, perPage, result);
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


    public CompletableFuture<Account> delete(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            Account foundAccount = findAccount(uuid);
            unitOfWork.getAccountRepository().delete(foundAccount);

            return foundAccount;
        });
    }

    public void activeAccount(Account account) {
        account.setStatus(AccountStatus.ACTIVE.name());
    }

    public AccountResponse verifyAccount(String email){
        Account account = unitOfWork.getAccountRepository().findByEmail(email);
        if (account != null){
            account.setStatus(AccountStatus.ACTIVE.name());
            unitOfWork.getAccountRepository().save(account);
        }
        return accountMapper.toResponse(account);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public Account findByUsername(String username) {
        return unitOfWork.getAccountRepository().findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    @Async("AsyncTaskExecutor")
    public CompletableFuture<List<Account>> findByUsernameContainingAsync(String username) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getAccountRepository().findByUsernameContaining(username)
        );
    }

    public Account findByEmail(String email) {
        if (unitOfWork.getAccountRepository().findByEmail(email) == null)
            throw new AppException(ErrorCode.ACCOUNT_NOT_FOUND);
        return unitOfWork.getAccountRepository().findByEmail(email);
    }
}
