package com.FA24SE088.OnlineForum.controller;

import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.FilterTransactionResponse;
import com.FA24SE088.OnlineForum.dto.response.SearchEverythingResponse;
import com.FA24SE088.OnlineForum.service.UtilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@RestController
@RequestMapping("/category")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UtilityController {
    UtilityService utilityService;

    @Operation(summary = "Search Account, Category, Topic, Post By 1 Keyword")
    @GetMapping(path = "/search")
    public ApiResponse<SearchEverythingResponse> searchEverything(@RequestParam String keyword,
                                                                  @RequestParam(required = false, defaultValue = "false") boolean isOnlyAccountIncluded,
                                                                  @RequestParam(required = false, defaultValue = "false") boolean isOnlyCategoryIncluded,
                                                                  @RequestParam(required = false, defaultValue = "false") boolean isOnlyTopicIncluded,
                                                                  @RequestParam(required = false, defaultValue = "false") boolean isOnlyPostIncluded) {
        return utilityService.searchEverything(keyword, isOnlyAccountIncluded, isOnlyCategoryIncluded,
                isOnlyTopicIncluded, isOnlyPostIncluded).thenApply(searchEverythingResponse ->
                ApiResponse.<SearchEverythingResponse>builder()
                        .entity(searchEverythingResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Filter Transaction, DailyPoint, and BonusPoint based on criteria")
    @GetMapping("/filter-transaction")
    public CompletableFuture<ApiResponse<FilterTransactionResponse>> filterEverythingInTransaction(
            @RequestParam(required = false, defaultValue = "false") boolean viewTransaction,
            @RequestParam(required = false, defaultValue = "false") boolean dailyPoint,
            @RequestParam(required = false, defaultValue = "false") boolean bonusPoint,
            @Parameter(description = "Filter by date in yyyy-MM-dd format", example = "2023-10-01")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "Filter by date in yyyy-MM-dd format", example = "2023-10-01")
            @RequestParam(required = false) String endDate) {

        return utilityService.filter(viewTransaction, dailyPoint, bonusPoint, startDate, endDate)
                .thenApply(filterTransactionResponse -> ApiResponse.<FilterTransactionResponse>builder()
                        .entity(filterTransactionResponse)
                        .build());
    }


}
