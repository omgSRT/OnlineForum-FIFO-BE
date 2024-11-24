package com.FA24SE088.OnlineForum.controller;

import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.DoDResponse;
import com.FA24SE088.OnlineForum.dto.response.ReportResponse;
import com.FA24SE088.OnlineForum.enums.ReportPostStatus;
import com.FA24SE088.OnlineForum.service.StatisticService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/statistic")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StatisticController {
    StatisticService statisticService;

    @Operation(summary = "Get DoD Statistic")
    @GetMapping(path = "/get/dod-statistic")
    public ApiResponse<DoDResponse> getAllReports(){
        return statisticService.getDodStatistic().thenApply(dodResponse ->
                ApiResponse.<DoDResponse>builder()
                        .entity(dodResponse)
                        .build()
        ).join();
    }
}
