package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.CategoryNoAccountRequest;
import com.FA24SE088.OnlineForum.dto.request.CategoryRequest;
import com.FA24SE088.OnlineForum.dto.request.CategoryUpdateAccountRequest;
import com.FA24SE088.OnlineForum.dto.request.CategoryUpdateRequest;
import com.FA24SE088.OnlineForum.dto.response.CategoryNoAccountResponse;
import com.FA24SE088.OnlineForum.dto.response.CategoryResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Category;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.CategoryMapper;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import com.FA24SE088.OnlineForum.utils.PaginationUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class CategoryService {
    UnitOfWork unitOfWork;
    PaginationUtils paginationUtils;
    CategoryMapper categoryMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<CategoryResponse> createCategory(CategoryRequest request) {
        var accountFuture = findAccountById(request.getAccountId());

        return accountFuture.thenCompose(acc ->
                unitOfWork.getCategoryRepository().existsByNameContaining(request.getName())
                .thenCompose(exists -> {
                    if (exists) {
                        throw new AppException(ErrorCode.NAME_EXIST);
                    }

                    Category newCategory = categoryMapper.toCategory(request);
                    newCategory.setAccount(acc);
                    return CompletableFuture.completedFuture(
                            categoryMapper.toCategoryResponse(unitOfWork.getCategoryRepository().save(newCategory))
                    );
                }));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<CategoryResponse> createCategoryNoAccount(CategoryNoAccountRequest request) {

        return unitOfWork.getCategoryRepository().existsByNameContaining(request.getName())
                        .thenCompose(exists -> {
                            if (exists) {
                                throw new AppException(ErrorCode.NAME_EXIST);
                            }

                            Category newCategory = categoryMapper.toCategoryWithNoAccount(request);
                            return CompletableFuture.completedFuture(
                                    categoryMapper.toCategoryResponse(unitOfWork.getCategoryRepository().save(newCategory))
                            );
                        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<List<CategoryNoAccountResponse>> getAllCategories(int page, int perPage) {
        return CompletableFuture.supplyAsync(() -> {
            var list = unitOfWork.getCategoryRepository().findAll().stream()
                    .map(categoryMapper::toCategoryNoAccountResponse)
                    .toList();

            return paginationUtils.convertListToPage(page, perPage, list);
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<List<CategoryNoAccountResponse>> getAllCategoriesByAccountId(int page, int perPage, UUID accountId) {
        var accountFuture = findAccountById(accountId);

        return accountFuture.thenCompose(account ->
                unitOfWork.getCategoryRepository()
                        .findByAccountAccountId(account.getAccountId())
                        .thenApply(list -> {
                            var responses = list.stream()
                                    .map(categoryMapper::toCategoryNoAccountResponse)
                                    .toList();
                            return paginationUtils.convertListToPage(page, perPage, responses);
                        }));
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public CompletableFuture<CategoryResponse> getCategoryById(UUID categoryId) {
        return CompletableFuture.supplyAsync(() -> {
            var category = unitOfWork.getCategoryRepository().findById(categoryId)
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

            return categoryMapper.toCategoryResponse(category);
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<CategoryResponse> deleteCategoryById(UUID categoryId){
        return CompletableFuture.supplyAsync(() -> {
            var category = unitOfWork.getCategoryRepository().findById(categoryId)
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

            unitOfWork.getCategoryRepository().delete(category);

            return categoryMapper.toCategoryResponse(category);
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<CategoryResponse> updateCategoryById(UUID categoryId, CategoryUpdateRequest request) {
        return unitOfWork.getCategoryRepository().existsByNameContaining(request.getName())
                .thenCompose(exists -> {
                    if (exists) {
                        throw new AppException(ErrorCode.NAME_EXIST);
                    }

                    return CompletableFuture.supplyAsync(() -> {
                        var category = unitOfWork.getCategoryRepository().findById(categoryId)
                                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

                        categoryMapper.updateCategory(category, request);
                        return unitOfWork.getCategoryRepository().save(category);
                    }).thenApply(categoryMapper::toCategoryResponse);
                });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<CategoryResponse> assignCategoryToAccountById(UUID categoryId, CategoryUpdateAccountRequest request) {
        var accountFuture = findAccountById(request.getAccountId());

        return accountFuture.thenApply(account -> {
            var category = unitOfWork.getCategoryRepository().findById(categoryId)
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

            category.setAccount(account);

            return categoryMapper.toCategoryResponse(unitOfWork.getCategoryRepository().save(category));
        });
    }


    @Async("AsyncTaskExecutor")
    private CompletableFuture<Account> findAccountById(UUID accountId) {
        return CompletableFuture.supplyAsync(() ->
                unitOfWork.getAccountRepository().findById(accountId)
                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND))
        );
    }

}
