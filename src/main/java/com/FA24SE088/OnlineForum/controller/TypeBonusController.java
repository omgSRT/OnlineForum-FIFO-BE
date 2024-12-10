package com.FA24SE088.OnlineForum.controller;

import com.FA24SE088.OnlineForum.dto.request.TypeBonusRequest;
import com.FA24SE088.OnlineForum.dto.request.TypeBonusUpdateRequest;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.TypeBonusResponse;
import com.FA24SE088.OnlineForum.enums.SuccessReturnMessage;
import com.FA24SE088.OnlineForum.service.TypeBonusService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/type-bonus")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TypeBonusController {
    TypeBonusService typeBonusService;

    @Operation(summary = "Create New Type Bonus")
    @PostMapping(path = "/create")
    public ApiResponse<TypeBonusResponse> createTypeBonus(@RequestBody @Valid TypeBonusRequest request) {
        return typeBonusService.createTypeBonus(request).thenApply(typeBonusResponse ->
                ApiResponse.<TypeBonusResponse>builder()
                        .message(SuccessReturnMessage.CREATE_SUCCESS.getMessage())
                        .entity(typeBonusResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Get All Type Bonuses")
    @GetMapping(path = "/getall")
    public ApiResponse<List<TypeBonusResponse>> getAllTypeBonuses(@RequestParam(defaultValue = "1") int page,
                                                                  @RequestParam(defaultValue = "10") int perPage,
                                                                  @RequestParam(required = false) String name) {
        return typeBonusService.getAllTypeBonuses(page, perPage, name).thenApply(typeBonusResponses ->
                ApiResponse.<List<TypeBonusResponse>>builder()
                        .entity(typeBonusResponses)
                        .build()
        ).join();
    }

    @Operation(summary = "Get Type Bonus", description = "Get Type Bonus By ID")
    @GetMapping(path = "/get/{typeBonusId}")
    public ApiResponse<TypeBonusResponse> getTypeBonusById(@PathVariable UUID typeBonusId) {
        return typeBonusService.getTypeBonusById(typeBonusId).thenApply(typeBonusResponse ->
                ApiResponse.<TypeBonusResponse>builder()
                        .entity(typeBonusResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Update Type Bonus", description = "Update Type Bonus By ID")
    @PutMapping(path = "/update/{typeBonusId}")
    public ApiResponse<TypeBonusResponse> updateTypeBonusById(@PathVariable UUID typeBonusId,
                                                              @RequestBody @Valid TypeBonusUpdateRequest request) {
        return typeBonusService.updateTypeBonusById(typeBonusId, request).thenApply(typeBonusResponse ->
                ApiResponse.<TypeBonusResponse>builder()
                        .message(SuccessReturnMessage.UPDATE_SUCCESS.getMessage())
                        .entity(typeBonusResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Delete Type Bonus", description = "Delete Type Bonus By ID")
    @DeleteMapping(path = "/delete/{typeBonusId}")
    public ApiResponse<TypeBonusResponse> deleteTypeBonusById(@PathVariable UUID typeBonusId) {
        return typeBonusService.deleteTypeBonus(typeBonusId).thenApply(typeBonusResponse ->
                ApiResponse.<TypeBonusResponse>builder()
                        .message(SuccessReturnMessage.DELETE_SUCCESS.getMessage())
                        .entity(typeBonusResponse)
                        .build()
        ).join();
    }
}
