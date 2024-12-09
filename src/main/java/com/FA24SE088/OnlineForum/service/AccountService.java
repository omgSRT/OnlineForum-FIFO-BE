package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.AccountUpdateCategoryRequest;
import com.FA24SE088.OnlineForum.dto.request.AccountUpdateInfoRequest;
import com.FA24SE088.OnlineForum.dto.request.AccountUpdateRequest;
import com.FA24SE088.OnlineForum.dto.request.AccountRequest;
import com.FA24SE088.OnlineForum.dto.response.AccountResponse;
import com.FA24SE088.OnlineForum.dto.response.RecommendAccountResponse;
import com.FA24SE088.OnlineForum.entity.*;
import com.FA24SE088.OnlineForum.enums.AccountStatus;

import com.FA24SE088.OnlineForum.enums.RoleAccount;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.AccountMapper;

import com.FA24SE088.OnlineForum.repository.*;
import com.FA24SE088.OnlineForum.utils.PaginationUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;


import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;


import org.springframework.security.access.prepost.PreAuthorize;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class AccountService {
    AccountRepository accountRepository;
    RoleRepository roleRepository;
    CategoryRepository categoryRepository;
    WalletRepository walletRepository;
    FollowRepository followRepository;
    BlockedAccountRepository blockedAccountRepository;
    AccountMapper accountMapper;
    PasswordEncoder passwordEncoder;
    PaginationUtils paginationUtils;

    public AccountResponse create(AccountRequest request) {
        if (accountRepository.existsByUsername(request.getUsername()))
            throw new AppException(ErrorCode.ACCOUNT_IS_EXISTED);
        if (accountRepository.existsByEmail(request.getEmail()))
            throw new AppException(ErrorCode.EMAIL_IS_EXISTED);
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }
        Account account = accountMapper.toAccount(request);
        account.setHandle(request.getUsername());
        account.setPassword(passwordEncoder.encode(request.getPassword()));

        if (!request.getRole().name().isEmpty() && request.getRole().name().equalsIgnoreCase("STAFF")) {
            Role role = roleRepository.findByName(request.getRole().name());
            if (role == null) throw new AppException(ErrorCode.ROLE_NOT_FOUND);
            account.setRole(role);
            if (request.getCategoryList_ForStaff() != null && !request.getCategoryList_ForStaff().isEmpty()) {
                List<Category> categories = new ArrayList<>();
                request.getCategoryList_ForStaff().forEach(categoryName -> {
                    Category categoryEntity = categoryRepository
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
            Role role = roleRepository.findByName("USER");
            if (role == null) throw new AppException(ErrorCode.ROLE_NOT_FOUND);
            account.setRole(role);
            account.setCategoryList(null);
        }

        Wallet wallet = new Wallet();
        wallet.setBalance(0);
        wallet.setAccount(account);
        account.setWallet(wallet);

        account.setCreatedDate(LocalDateTime.now());
        account.setStatus(AccountStatus.PENDING_APPROVAL.name());
//        String handle = String.format("@%s", request.getUsername());
        String handle = String.format(request.getUsername());
        account.setHandle(handle);
        accountRepository.save(account);
        AccountResponse response = accountMapper.toResponse(account);
        response.setAccountId(account.getAccountId());
        return response;
    }

    public AccountResponse callbackLoginGoogle(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String avatar = oAuth2User.getAttribute("picture");
        String username = email.split("@")[0];

        if (accountRepository.existsByEmail(email)) {
            Account existingAccount = accountRepository.findByEmail(email);
            if (existingAccount != null) throw new AppException(ErrorCode.EMAIL_IS_EXISTED);
        }

        Account newAccount = new Account();
        newAccount.setEmail(email);
        newAccount.setUsername(username);
        newAccount.setAvatar(avatar);
        newAccount.setStatus(AccountStatus.ACTIVE.name());
        newAccount.setHandle(String.format("@%s", username));
        newAccount.setCreatedDate(LocalDateTime.now());

        Role role = roleRepository.findByName("USER");
        if (role == null) throw new AppException(ErrorCode.ROLE_NOT_FOUND);
        newAccount.setRole(role);

        Wallet wallet = new Wallet();
        wallet.setBalance(0);
        wallet.setAccount(newAccount);
        newAccount.setWallet(wallet);

        accountRepository.save(newAccount);
        walletRepository.save(wallet);

        return accountMapper.toResponse(newAccount);
    }

    public AccountResponse updateCategoryOfStaff(UUID id, AccountUpdateCategoryRequest request) {
        Account account = findAccount(id);

        if (account.getRole().getName().equals("STAFF")) {
            List<Category> currentCategoryList = account.getCategoryList();
            List<Category> newCategoryList = new ArrayList<>();
            request.getCategoryList().forEach(cateName -> {
                Category category = categoryRepository.findByName(cateName)
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
                    categoryRepository.save(oldCategory);
                }
            }
            // Xóa các category cũ trong danh sách hiện tại
            currentCategoryList.clear();

            // Thêm tất cả category mới vào danh sách hiện tại
            currentCategoryList.addAll(newCategoryList);

            accountRepository.save(account);
        }

        return accountMapper.toResponse(account);
    }

    public AccountResponse updateInfo(AccountUpdateInfoRequest request) {
        Account account = getCurrentUser();
        if (request.getOldPassword() != null && !request.getOldPassword().isEmpty()) {
            if (!passwordEncoder.matches(request.getOldPassword(), account.getPassword())) {
                throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
            }

        }
        if (request.getNewPass() != null && !request.getNewPass().isEmpty()) {
            // Mã hóa mật khẩu mới (encoder là mã hoá 1 chiều)
            account.setPassword(passwordEncoder.encode(request.getNewPass()));
        }
        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            account.setAvatar(request.getAvatar());
        }
        if (request.getCoverImage() != null && !request.getCoverImage().isEmpty()) {
            account.setCoverImage(request.getCoverImage());
        }
        if (request.getBio() != null && !request.getBio().isEmpty()) {
            account.setBio(request.getBio());
        }
        if (request.getHandle() != null && !request.getHandle().isEmpty()) {
            String handle = String.format("@%s", request.getHandle());
            account.setHandle(handle);
        }
        accountRepository.save(account);
        return accountMapper.toResponse(account);
    }

    private Account getCurrentUser() {
        var context = SecurityContextHolder.getContext();
        return accountRepository.findByUsername(context.getAuthentication().getName()).orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    public List<AccountResponse> getAll(int page, int perPage) {
        var list = accountRepository.findAll().stream()
                .map(account -> {
                    long followerCount = followRepository.countByFollowee(account);
                    long followeeCount = followRepository.countByFollower(account);

                    AccountResponse response = accountMapper.toResponse(account);
                    response.setCountFollowee(followeeCount);
                    response.setCountFollower(followerCount);

                    return response;
                })
                .toList();
        return paginationUtils.convertListToPage(page, perPage, list);
    }

    public List<AccountResponse> filter(int page, int perPage, String username, String email, AccountStatus status, RoleAccount role) {
        List<AccountResponse> result = accountRepository.findAll().stream()
                .map(account -> {
                    long followerCount = followRepository.countByFollowee(account);
                    long followeeCount = followRepository.countByFollower(account);

                    AccountResponse response = accountMapper.toResponse(account);
                    response.setCountFollowee(followeeCount);
                    response.setCountFollower(followerCount);

                    return response;
                })
                .filter(x -> (username == null || (x.getUsername() != null && x.getUsername().contains(username))))
                .filter(x -> (email == null || (x.getEmail() != null && x.getEmail().contains(email))))
                .filter(x -> (status == null || (x.getStatus() != null && x.getStatus().contains(status.name()))))
                .filter(x -> (role == null || (x.getRole() != null && x.getRole().getName().contains(role.name()))))
                .toList();
        return paginationUtils.convertListToPage(page, perPage, result);
    }

    private Account findAccount(UUID id) {
        return accountRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    public AccountResponse update(UUID id, AccountUpdateRequest request) {
        Account account = findAccount(id);
        if (account != null) {
            accountMapper.updateAccount(account, request);
            accountRepository.save(account);
        }
        return accountMapper.toResponse(account);
    }

    public AccountResponse findById(UUID id) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        long followerCount = followRepository.countByFollowee(account);
        long followeeCount = followRepository.countByFollower(account);

        AccountResponse response = accountMapper.toResponse(account);
        response.setCountFollowee(followeeCount);
        response.setCountFollower(followerCount);

        return response;
    }

    public CompletableFuture<Account> delete(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            Account foundAccount = findAccount(uuid);
            accountRepository.delete(foundAccount);

            return foundAccount;
        });
    }

    public AccountResponse verifyAccount(String email) {
        Account account = accountRepository.findByEmail(email);
        if (account != null) {
            account.setStatus(AccountStatus.ACTIVE.name());
            accountRepository.save(account);
        }
        return accountMapper.toResponse(account);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public Account findByUsername(String username) {
        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    @Async("AsyncTaskExecutor")
    public CompletableFuture<List<Account>> findByUsernameContainingAsync(String username) {
        return CompletableFuture.supplyAsync(() ->
                accountRepository.findByUsernameContaining(username)
        );
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    @Async("AsyncTaskExecutor")
    public CompletableFuture<List<RecommendAccountResponse>> getRecommendedAccounts(int page, int perPage) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -48);
        Date last48hours = calendar.getTime();
        var recommendedAccountsFuture = accountRepository.findRecommendedAccounts(last48hours);

        var currentFolloweesFuture = getFolloweeList();
        var username = getUsernameFromJwt();
        var currentAccountFuture = findAccountByUsername(username);
        var blockedListFuture = getBlockedAccountListByUsername(username);
        var blockerListFuture = getBlockerAccountListByUsername(username);

        return CompletableFuture.allOf(recommendedAccountsFuture, currentFolloweesFuture, blockedListFuture,
                blockerListFuture).thenCompose(v -> {
            var recommendedAccounts = recommendedAccountsFuture.join();
            var currentAccount = currentAccountFuture.join();
            var currentFollowees = currentFolloweesFuture.join();
            var blockedList = blockedListFuture.join();
            var blockerList = blockerListFuture.join();

            var list = recommendedAccounts.stream()
                    .filter(response -> !response.getAccount().equals(currentAccount))
                    .filter(response -> response.getAccount().getStatus().equalsIgnoreCase(AccountStatus.ACTIVE.name()))
                    .filter(response -> !currentFollowees.contains(response.getAccount()))
                    .filter(response -> !blockedList.contains(response.getAccount())
                            && !blockerList.contains(response.getAccount()))
                    .sorted((response1, response2) -> Long.compare(response2.getTrendScore(), response1.getTrendScore()))
                    .toList();

            return CompletableFuture.completedFuture(paginationUtils.convertListToPage(page, perPage, list));
        });
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

    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<Account>> getFolloweeList() {
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);
        return accountFuture.thenCompose(account -> {
            var followeeList = followRepository.findByFollower(account).stream()
                    .map(Follow::getFollowee)
                    .toList();

            return CompletableFuture.completedFuture(followeeList);
        });
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<Account>> getBlockedAccountListByUsername(String username) {
        var accountFuture = findAccountByUsername(username);

        return accountFuture.thenApply(account -> {
            var blockedAccountEntityList = blockedAccountRepository.findByBlocker(account);

            return blockedAccountEntityList.stream()
                    .map(BlockedAccount::getBlocked)
                    .toList();
        });
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<List<Account>> getBlockerAccountListByUsername(String username) {
        var accountFuture = findAccountByUsername(username);

        return accountFuture.thenApply(account -> {
            var blockedAccountEntityList = blockedAccountRepository.findByBlocked(account);

            return blockedAccountEntityList.stream()
                    .map(BlockedAccount::getBlocker)
                    .toList();
        });
    }
}
