package com.FA24SE088.OnlineForum.controller;

import com.FA24SE088.OnlineForum.dto.request.CategoryRequest;
import com.FA24SE088.OnlineForum.dto.request.CategoryUpdateRequest;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.CategoryNoAccountResponse;
import com.FA24SE088.OnlineForum.dto.response.CategoryResponse;
import com.FA24SE088.OnlineForum.enums.SuccessReturnMessage;
import com.FA24SE088.OnlineForum.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {
    final CategoryService categoryService;

    @Operation(summary = "Create New Category")
    @PostMapping(path = "/create")
    public ApiResponse<CategoryResponse> createCategory(@RequestBody @Valid CategoryRequest request){
        return categoryService.createCategory(request).thenApply(categoryResponse ->
                ApiResponse.<CategoryResponse>builder()
                        .message(SuccessReturnMessage.CREATE_SUCCESS.getMessage())
                        .entity(categoryResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Get All Categories")
    @GetMapping(path = "/getall")
    public ApiResponse<List<CategoryNoAccountResponse>> getAllCategories(@RequestParam(defaultValue = "1") int page,
                                                                         @RequestParam(defaultValue = "10") int perPage){
        return categoryService.getAllCategories(page, perPage).thenApply(categoryNoAccountResponses ->
                ApiResponse.<List<CategoryNoAccountResponse>>builder()
                        .entity(categoryNoAccountResponses)
                        .build()
        ).join();
    }

    @Operation(summary = "Get A Category", description = "Get A Category By ID")
    @GetMapping(path = "/get/{categoryId}")
    public ApiResponse<CategoryResponse> getCategoryById(@PathVariable UUID categoryId){
        return categoryService.getCategoryById(categoryId).thenApply(categoryResponse ->
                ApiResponse.<CategoryResponse>builder()
                        .message(SuccessReturnMessage.SEARCH_SUCCESS.getMessage())
                        .entity(categoryResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Delete A Category", description = "Delete A Category By ID")
    @DeleteMapping(path = "/delete/{categoryId}")
    public ApiResponse<CategoryResponse> deleteCategoryById(@PathVariable UUID categoryId){
        return categoryService.deleteCategoryById(categoryId).thenApply(categoryResponse ->
                ApiResponse.<CategoryResponse>builder()
                        .message(SuccessReturnMessage.DELETE_SUCCESS.getMessage())
                        .entity(categoryResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Update A Category", description = "Update A Category By ID")
    @PutMapping(path = "/update/{categoryId}")
    public ApiResponse<CategoryResponse> updateCategoryById(@PathVariable UUID categoryId,
                                                            @RequestBody @Valid CategoryUpdateRequest request){
        return categoryService.updateCategoryById(categoryId, request).thenApply(categoryResponse ->
                ApiResponse.<CategoryResponse>builder()
                        .message(SuccessReturnMessage.UPDATE_SUCCESS.getMessage())
                        .entity(categoryResponse)
                        .build()
        ).join();
    }
}
