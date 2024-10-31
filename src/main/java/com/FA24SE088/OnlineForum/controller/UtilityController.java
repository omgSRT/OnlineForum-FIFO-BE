package com.FA24SE088.OnlineForum.controller;

import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.SearchEverythingResponse;
import com.FA24SE088.OnlineForum.service.UtilityService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
                                                                  @RequestParam(required = false, defaultValue = "false") boolean isOnlyPostIncluded){
        return utilityService.searchEverything(keyword, isOnlyAccountIncluded, isOnlyCategoryIncluded,
                isOnlyTopicIncluded, isOnlyPostIncluded).thenApply(searchEverythingResponse ->
                        ApiResponse.<SearchEverythingResponse>builder()
                                .entity(searchEverythingResponse)
                                .build()
        ).join();
    }
}
