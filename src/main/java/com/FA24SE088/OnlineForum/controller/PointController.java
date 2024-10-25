package com.FA24SE088.OnlineForum.controller;

import com.FA24SE088.OnlineForum.dto.request.PointRequest;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.PointResponse;
import com.FA24SE088.OnlineForum.enums.SuccessReturnMessage;
import com.FA24SE088.OnlineForum.service.PointService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/point")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PointController {
    PointService pointService;

    @Operation(summary = "Create New Point Data")
    @PostMapping(path = "/create")
    public ApiResponse<PointResponse> createPoint(@RequestBody @Valid PointRequest request){
        return pointService.createPoint(request).thenApply(pointResponse ->
            ApiResponse.<PointResponse>builder()
                    .message(SuccessReturnMessage.CREATE_SUCCESS.getMessage())
                    .entity(pointResponse)
                    .build()
        ).join();
    }

    @Operation(summary = "Get All Point Data")
    @GetMapping(path = "/getall")
    public ApiResponse<List<PointResponse>> getAllPoint(){
        return pointService.getAllPoints().thenApply(pointResponses ->
                ApiResponse.<List<PointResponse>>builder()
                        .entity(pointResponses)
                        .build()
        ).join();
    }

    @Operation(summary = "Update Point Data")
    @PutMapping(path = "/update")
    public ApiResponse<PointResponse> updatePoint(@RequestBody @Valid PointRequest request){
        return pointService.updatePoint(request).thenApply(pointResponse ->
            ApiResponse.<PointResponse>builder()
                    .message(SuccessReturnMessage.UPDATE_SUCCESS.getMessage())
                    .entity(pointResponse)
                    .build()
        ).join();
    }

    @Operation(summary = "Delete Point Data")
    @DeleteMapping(path = "/delete")
    public ApiResponse<PointResponse> deletePoint(){
        return pointService.deletePoint().thenApply(pointResponse ->
                ApiResponse.<PointResponse>builder()
                        .message(SuccessReturnMessage.DELETE_SUCCESS.getMessage())
                        .entity(pointResponse)
                        .build()
        ).join();
    }
}
