package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.CategoryNoAccountRequest;
import com.FA24SE088.OnlineForum.dto.request.CategoryRequest;
import com.FA24SE088.OnlineForum.dto.request.CategoryUpdateAccountRequest;
import com.FA24SE088.OnlineForum.dto.request.CategoryUpdateRequest;
import com.FA24SE088.OnlineForum.dto.response.CategoryGetAllResponse;
import com.FA24SE088.OnlineForum.dto.response.CategoryResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.entity.Category;
import com.FA24SE088.OnlineForum.entity.Topic;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.CategoryMapper;
import com.FA24SE088.OnlineForum.repository.*;
import com.FA24SE088.OnlineForum.utils.PaginationUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class CategoryService {
    CategoryRepository categoryRepository;
    TopicRepository topicRepository;
    UpvoteRepository upvoteRepository;
    CommentRepository commentRepository;
    PostViewRepository postViewRepository;
    AccountRepository accountRepository;
    PaginationUtils paginationUtils;
    CategoryMapper categoryMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<CategoryResponse> createCategory(CategoryRequest request) {
        var accountFuture = findAccountById(request.getAccountId());

        return accountFuture.thenCompose(acc ->
                categoryRepository.existsByNameContaining(request.getName())
                        .thenCompose(exists -> {
                            if (exists) {
                                throw new AppException(ErrorCode.NAME_EXIST);
                            }

                            request.setName(request.getName().toUpperCase());

                            Category newCategory = categoryMapper.toCategory(request);
                            newCategory.setAccount(acc);

                            var savedCategory = categoryRepository.save(newCategory);

                            CompletableFuture<Integer> upvoteCountFuture = upvoteRepository
                                    .countByPostTopicCategory(savedCategory);
                            CompletableFuture<Integer> commentCountFuture = commentRepository
                                    .countByPostTopicCategory(savedCategory);
                            CompletableFuture<Integer> viewCountFuture = postViewRepository
                                    .countByPostTopicCategory(savedCategory);

                            return CompletableFuture.allOf(upvoteCountFuture, commentCountFuture, viewCountFuture)
                                    .thenApply(voidResult -> {
                                        CategoryResponse response = categoryMapper.toCategoryResponse(savedCategory);
                                        response.setUpvoteCount(upvoteCountFuture.join());
                                        response.setCommentCount(commentCountFuture.join());
                                        response.setViewCount(viewCountFuture.join());
                                        return response;
                                    });
                        }));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<CategoryResponse> createCategoryNoAccount(CategoryNoAccountRequest request) {

        return categoryRepository.existsByNameContaining(request.getName())
                .thenCompose(exists -> {
                    if (exists) {
                        throw new AppException(ErrorCode.NAME_EXIST);
                    }

                    request.setName(request.getName().toUpperCase());

                    Category newCategory = categoryMapper.toCategoryWithNoAccount(request);

                    var savedCategory = categoryRepository.save(newCategory);

                    CompletableFuture<Integer> upvoteCountFuture = upvoteRepository
                            .countByPostTopicCategory(savedCategory);
                    CompletableFuture<Integer> commentCountFuture = commentRepository
                            .countByPostTopicCategory(savedCategory);
                    CompletableFuture<Integer> viewCountFuture = postViewRepository
                            .countByPostTopicCategory(savedCategory);

                    return CompletableFuture.allOf(upvoteCountFuture, commentCountFuture, viewCountFuture)
                            .thenApply(voidResult -> {
                                CategoryResponse response = categoryMapper.toCategoryResponse(savedCategory);
                                response.setUpvoteCount(upvoteCountFuture.join());
                                response.setCommentCount(commentCountFuture.join());
                                response.setViewCount(viewCountFuture.join());
                                return response;
                            });
                });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('USER')")
    public CompletableFuture<List<CategoryGetAllResponse>> getAllCategories(int page, int perPage, UUID accountId) {
        var accountFuture = accountId != null
                ? findAccountById(accountId)
                : CompletableFuture.completedFuture(null);

        return accountFuture.thenCompose(account -> {
            var categories = categoryRepository.findAll();

            List<String> customOrder = List.of("KNOWLEDGE SHARING", "SOURCE CODE");

            categories.sort((c1, c2) -> {
                int index1 = customOrder.indexOf(c1.getName());
                int index2 = customOrder.indexOf(c2.getName());

                // If both categories are in the custom order, compare by their indices
                if (index1 != -1 && index2 != -1) {
                    return Integer.compare(index1, index2);
                }
                // If one is in the custom order, it comes before the other
                if (index1 != -1) return -1;
                if (index2 != -1) return 1;
                // If neither is in the custom order, sort alphabetically by name
                return c1.getName().compareTo(c2.getName());
            });

            List<CompletableFuture<CategoryGetAllResponse>> responseFutures = categories.stream()
                    .filter(category -> account == null || (category.getAccount() != null && category.getAccount().equals(account)))
                    .map(category -> {
                        CompletableFuture<Integer> upvoteCountFuture = upvoteRepository
                                .countByPostTopicCategory(category);
                        CompletableFuture<Integer> commentCountFuture = commentRepository
                                .countByPostTopicCategory(category);
                        CompletableFuture<List<Topic>> topicListByCategoryFuture = topicRepository
                                .findByCategoryCategoryId(category.getCategoryId());
                        CompletableFuture<Integer> viewCountFuture = postViewRepository
                                .countByPostTopicCategory(category);

                        return CompletableFuture.allOf(upvoteCountFuture, commentCountFuture, topicListByCategoryFuture, viewCountFuture)
                                .thenApply(voidResult -> {
                                    CategoryGetAllResponse response = categoryMapper.toCategoryGetAllResponse(category);
                                    response.setUpvoteCount(upvoteCountFuture.join());
                                    response.setCommentCount(commentCountFuture.join());
                                    response.setTopicListByCategory(topicListByCategoryFuture.join());
                                    response.setViewCount(viewCountFuture.join());
                                    return response;
                                });
                    })
                    .toList();

            return CompletableFuture.allOf(responseFutures.toArray(new CompletableFuture[0]))
                    .thenApply(voidResult -> responseFutures.stream()
                            .map(CompletableFuture::join)
                            .toList()
                    )
                    .thenApply(list -> paginationUtils.convertListToPage(page, perPage, list));
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public CompletableFuture<List<CategoryGetAllResponse>> getAllCategoriesForStaff(int page, int perPage) {
        var username = getUsernameFromJwt();
        var accountFuture = findAccountByUsername(username);

        return accountFuture.thenCompose(account -> {
            var categoryListFuture = categoryRepository.findByAccount(account);

            return categoryListFuture.thenCompose(categoryList -> {
                List<String> customOrder = List.of("KNOWLEDGE SHARING", "SOURCE CODE");

                categoryList.sort((c1, c2) -> {
                    int index1 = customOrder.indexOf(c1.getName());
                    int index2 = customOrder.indexOf(c2.getName());

                    // If both categories are in the custom order, compare by their indices
                    if (index1 != -1 && index2 != -1) {
                        return Integer.compare(index1, index2);
                    }
                    // If one is in the custom order, it comes before the other
                    if (index1 != -1) return -1;
                    if (index2 != -1) return 1;
                    // If neither is in the custom order, sort alphabetically by name
                    return c1.getName().compareTo(c2.getName());
                });

                List<CompletableFuture<CategoryGetAllResponse>> responseFutures = categoryList.stream()
                        .map(category -> {
                            CompletableFuture<Integer> upvoteCountFuture = upvoteRepository
                                    .countByPostTopicCategory(category);
                            CompletableFuture<Integer> commentCountFuture = commentRepository
                                    .countByPostTopicCategory(category);
                            CompletableFuture<List<Topic>> topicListByCategoryFuture = topicRepository
                                    .findByCategoryCategoryId(category.getCategoryId());
                            CompletableFuture<Integer> viewCountFuture = postViewRepository
                                    .countByPostTopicCategory(category);

                            return CompletableFuture.allOf(upvoteCountFuture, commentCountFuture, topicListByCategoryFuture, viewCountFuture)
                                    .thenApply(voidResult -> {
                                        CategoryGetAllResponse response = categoryMapper.toCategoryGetAllResponse(category);
                                        response.setUpvoteCount(upvoteCountFuture.join());
                                        response.setCommentCount(commentCountFuture.join());
                                        response.setTopicListByCategory(topicListByCategoryFuture.join());
                                        response.setViewCount(viewCountFuture.join());
                                        return response;
                                    });
                        })
                        .toList();

                return CompletableFuture.allOf(responseFutures.toArray(new CompletableFuture[0]))
                        .thenApply(voidResult -> responseFutures.stream()
                                .map(CompletableFuture::join)
                                .toList()
                        )
                        .thenApply(list -> paginationUtils.convertListToPage(page, perPage, list));
            });
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public CompletableFuture<CategoryResponse> getCategoryById(UUID categoryId) {
        return CompletableFuture.supplyAsync(() ->
                categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND))
        ).thenCompose(category -> {
            CompletableFuture<Integer> upvoteCountFuture = upvoteRepository
                    .countByPostTopicCategory(category);
            CompletableFuture<Integer> commentCountFuture = commentRepository
                    .countByPostTopicCategory(category);
            CompletableFuture<Integer> viewCountFuture = postViewRepository
                    .countByPostTopicCategory(category);

            return CompletableFuture.allOf(upvoteCountFuture, commentCountFuture, viewCountFuture)
                    .thenApply(voidResult -> {
                        CategoryResponse response = categoryMapper.toCategoryResponse(category);
                        response.setUpvoteCount(upvoteCountFuture.join());
                        response.setCommentCount(commentCountFuture.join());
                        response.setViewCount(viewCountFuture.join());
                        return response;
                    });
        });

    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<CategoryResponse> deleteCategoryById(UUID categoryId) {
        return CompletableFuture.supplyAsync(() -> {
            var category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

            categoryRepository.delete(category);

            return category;
        }).thenCompose(category -> {
            CompletableFuture<Integer> upvoteCountFuture = upvoteRepository
                    .countByPostTopicCategory(category);
            CompletableFuture<Integer> commentCountFuture = commentRepository
                    .countByPostTopicCategory(category);
            CompletableFuture<Integer> viewCountFuture = postViewRepository
                    .countByPostTopicCategory(category);

            return CompletableFuture.allOf(upvoteCountFuture, commentCountFuture, viewCountFuture)
                    .thenApply(voidResult -> {
                        CategoryResponse response = categoryMapper.toCategoryResponse(category);
                        response.setUpvoteCount(upvoteCountFuture.join());
                        response.setCommentCount(commentCountFuture.join());
                        response.setViewCount(viewCountFuture.join());
                        return response;
                    });
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<CategoryResponse> updateCategoryById(UUID categoryId, CategoryUpdateRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            var category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

            if (category.getName().equalsIgnoreCase(request.getName())) {
                return category;
            }

            boolean nameExists = categoryRepository.existsByNameContaining(request.getName()).join();
            if (nameExists) {
                throw new AppException(ErrorCode.NAME_EXIST);
            }
            return category;
        }).thenCompose(category -> {
            request.setName(request.getName() == null || request.getName().isEmpty()
                    ? category.getName()
                    : request.getName());
            request.setDescription(request.getDescription() == null || request.getDescription().isEmpty()
                    ? category.getDescription()
                    : request.getDescription());
            request.setImage(request.getImage() == null || request.getImage().isEmpty()
                    ? category.getDescription()
                    : request.getImage());
            categoryMapper.updateCategory(category, request);

            return CompletableFuture.completedFuture(categoryRepository.save(category));
        }).thenCompose(updatedCategory -> {
            CompletableFuture<Integer> upvoteCountFuture = upvoteRepository.countByPostTopicCategory(updatedCategory);
            CompletableFuture<Integer> commentCountFuture = commentRepository.countByPostTopicCategory(updatedCategory);
            CompletableFuture<Integer> viewCountFuture = postViewRepository.countByPostTopicCategory(updatedCategory);

            return CompletableFuture.allOf(upvoteCountFuture, commentCountFuture, viewCountFuture)
                    .thenApply(voidResult -> {
                        CategoryResponse response = categoryMapper.toCategoryResponse(updatedCategory);
                        response.setUpvoteCount(upvoteCountFuture.join());
                        response.setCommentCount(commentCountFuture.join());
                        response.setViewCount(viewCountFuture.join());
                        return response;
                    });
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<CategoryResponse> assignCategoryToAccountById(UUID categoryId, CategoryUpdateAccountRequest request) {
        var accountFuture = findAccountById(request.getAccountId());

        return accountFuture.thenCompose(account ->
                categoryRepository.findByAccount(account).thenCompose(categoryList -> {
                    var category = categoryRepository.findById(categoryId)
                            .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
                    if (categoryList.contains(category)) {
                        throw new AppException(ErrorCode.CATEGORY_HAS_UNDERTAKE);
                    }

                    category.setAccount(account);

                    return CompletableFuture.completedFuture(categoryRepository.save(category));
                }).thenCompose(category -> {
                    CompletableFuture<Integer> upvoteCountFuture = upvoteRepository
                            .countByPostTopicCategory(category);
                    CompletableFuture<Integer> commentCountFuture = commentRepository
                            .countByPostTopicCategory(category);
                    CompletableFuture<Integer> viewCountFuture = postViewRepository
                            .countByPostTopicCategory(category);

                    return CompletableFuture.allOf(upvoteCountFuture, commentCountFuture, viewCountFuture)
                            .thenApply(voidResult -> {
                                CategoryResponse response = categoryMapper.toCategoryResponse(category);
                                response.setUpvoteCount(upvoteCountFuture.join());
                                response.setCommentCount(commentCountFuture.join());
                                response.setViewCount(viewCountFuture.join());
                                return response;
                            });
                })
        );
    }


    @Async("AsyncTaskExecutor")
    private CompletableFuture<Account> findAccountById(UUID accountId) {
        return CompletableFuture.supplyAsync(() ->
                accountRepository.findById(accountId)
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
    private CompletableFuture<Account> findAccountByUsername(String username) {
        return CompletableFuture.supplyAsync(() ->
                accountRepository.findByUsername(username)
                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND))
        );
    }
}
