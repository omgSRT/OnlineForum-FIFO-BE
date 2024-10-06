package com.FA24SE088.OnlineForum.controller;

import com.FA24SE088.OnlineForum.dto.request.DailyPointRequest;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.DailyPointResponse;
import com.FA24SE088.OnlineForum.enums.SuccessReturnMessage;
import com.FA24SE088.OnlineForum.service.DailyPointService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/daily-point")
@Slf4j
public class DailyPointController {
    final DailyPointService dailyPointService;

    @Operation(summary = "Create New Daily Point Log")
    @PostMapping(path = "/create")
    public ApiResponse<DailyPointResponse> createDailyPoint(@RequestBody @Valid DailyPointRequest request){
        return dailyPointService.createDailyPoint(request).thenApply(dailyPointResponse ->
                ApiResponse.<DailyPointResponse>builder()
                        .message(SuccessReturnMessage.CREATE_SUCCESS.getMessage())
                        .entity(dailyPointResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Get All Daily Point Log")
    @GetMapping(path = "/getall")
    public ApiResponse<List<DailyPointResponse>> getAllDailyPoints(@RequestParam(defaultValue = "1") int page,
                                                                   @RequestParam(defaultValue = "10") int perPage){
        return dailyPointService.getAllDailyPoints(page, perPage).thenApply(dailyPointResponses ->
                ApiResponse.<List<DailyPointResponse>>builder()
                        .entity(dailyPointResponses)
                        .build()
        ).join();
    }

    @Operation(summary = "Get All Daily Point Log", description = "Get All By Account ID")
    @GetMapping(path = "/getall/by-account/{accountId}")
    public ApiResponse<List<DailyPointResponse>> getAllDailyPointsByAccountId(@RequestParam(defaultValue = "1") int page,
                                                                              @RequestParam(defaultValue = "10") int perPage,
                                                                              @PathVariable UUID accountId){
        return dailyPointService.getAllDailyPointsByAccountId(page, perPage, accountId).thenApply(dailyPointResponses ->
                ApiResponse.<List<DailyPointResponse>>builder()
                        .entity(dailyPointResponses)
                        .build()
        ).join();
    }

    @Operation(summary = "Get All Daily Point Log", description = "Get All By Given Date")
    @GetMapping(path = "/getall/by-created-date")
    public ApiResponse<List<DailyPointResponse>> getAllDailyPointsByCreatedDate(@RequestParam(defaultValue = "1") int page,
                                                                                @RequestParam(defaultValue = "10") int perPage,
                                                                                @RequestParam Date givenDate){
        return dailyPointService.getAllDailyPointsByDate(page, perPage, givenDate).thenApply(dailyPointResponses ->
                ApiResponse.<List<DailyPointResponse>>builder()
                        .entity(dailyPointResponses)
                        .build()
        ).join();
    }

    @Operation(summary = "Delete Daily Point Log", description = "Delete By Daily Point ID")
    @DeleteMapping(path = "/delete/{dailyPointId}")
    public ApiResponse<DailyPointResponse> deleteDailyPointById(@PathVariable UUID dailyPointId){
        return dailyPointService.deleteDailyPointPoint(dailyPointId).thenApply(dailyPointResponse ->
                ApiResponse.<DailyPointResponse>builder()
                        .message(SuccessReturnMessage.DELETE_SUCCESS.getMessage())
                        .entity(dailyPointResponse)
                        .build()
                ).join();
    }
}
