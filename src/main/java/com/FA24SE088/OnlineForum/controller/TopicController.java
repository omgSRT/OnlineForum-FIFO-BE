package com.FA24SE088.OnlineForum.controller;

import com.FA24SE088.OnlineForum.dto.request.TopicRequest;
import com.FA24SE088.OnlineForum.dto.request.TopicUpdateRequest;
import com.FA24SE088.OnlineForum.dto.response.*;
import com.FA24SE088.OnlineForum.enums.SuccessReturnMessage;
import com.FA24SE088.OnlineForum.service.TopicService;
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
@RequestMapping("/topic")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TopicController {
    TopicService topicService;

    @Operation(summary = "Create New Topic")
    @PostMapping(path = "/create")
    public ApiResponse<TopicResponse> createTopic(@RequestBody @Valid TopicRequest request){
        return topicService.createTopic(request).thenApply(topicResponse ->
                ApiResponse.<TopicResponse>builder()
                        .message(SuccessReturnMessage.CREATE_SUCCESS.getMessage())
                        .entity(topicResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Get All Topics")
    @GetMapping(path = "/getall")
    public ApiResponse<List<TopicResponse>> getAllTopics(@RequestParam(defaultValue = "1") int page,
                                                                   @RequestParam(defaultValue = "10") int perPage){
        return topicService.getAllTopics(page, perPage).thenApply(topicResponses ->
                ApiResponse.<List<TopicResponse>>builder()
                        .entity(topicResponses)
                        .build()
        ).join();
    }

    @Operation(summary = "Get All Topics")
    @GetMapping(path = "/popular")
    public ApiResponse<List<TopicResponse>> getAllPopularTopics(@RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int perPage){
        return topicService.getAllPopularTopics(page, perPage).thenApply(topicResponses ->
                ApiResponse.<List<TopicResponse>>builder()
                        .entity(topicResponses)
                        .build()
        ).join();
    }

    @Operation(summary = "Get All Topics", description = "Get All Topics Based On Category ID")
    @GetMapping(path = "/getall/by-category/{categoryId}")
    public ApiResponse<List<TopicResponse>> getAllTopicsByCategoryId(@RequestParam(defaultValue = "1") int page,
                                                                                    @RequestParam(defaultValue = "10") int perPage,
                                                                                    @PathVariable UUID categoryId){
        return topicService.getAllTopicsByCategoryId(page, perPage, categoryId).thenApply(topicResponses ->
                ApiResponse.<List<TopicResponse>>builder()
                        .entity(topicResponses)
                        .build()
        ).join();
    }

    @Operation(summary = "Get A Topic", description = "Get A Topic By ID")
    @GetMapping(path = "/get/{topicId}")
    public ApiResponse<TopicResponse> getTopicById(@PathVariable UUID topicId){
        return topicService.getTopicById(topicId).thenApply(topicResponse ->
                ApiResponse.<TopicResponse>builder()
                        .message(SuccessReturnMessage.SEARCH_SUCCESS.getMessage())
                        .entity(topicResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Delete A Topic", description = "Delete A Topic By ID")
    @DeleteMapping(path = "/delete/{topicId}")
    public ApiResponse<TopicResponse> deleteTopicById(@PathVariable UUID topicId){
        return topicService.deleteTopicById(topicId).thenApply(topicResponse ->
                ApiResponse.<TopicResponse>builder()
                        .message(SuccessReturnMessage.DELETE_SUCCESS.getMessage())
                        .entity(topicResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Update A Topic", description = "Update A Topic By ID")
    @PutMapping(path = "/update/{topicId}")
    public ApiResponse<TopicResponse> updateTopicById(@PathVariable UUID topicId,
                                                            @RequestBody @Valid TopicUpdateRequest request){
        return topicService.updateTopicById(topicId, request).thenApply(topicResponse ->
                ApiResponse.<TopicResponse>builder()
                        .message(SuccessReturnMessage.UPDATE_SUCCESS.getMessage())
                        .entity(topicResponse)
                        .build()
        ).join();
    }
}
