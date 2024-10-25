package com.FA24SE088.OnlineForum.controller;

import com.FA24SE088.OnlineForum.dto.request.TagRequest;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.TagResponse;
import com.FA24SE088.OnlineForum.enums.SuccessReturnMessage;
import com.FA24SE088.OnlineForum.service.TagService;
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
@RequestMapping("/tag")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TagController {
    TagService tagService;

    @Operation(summary = "Create New Tag")
    @PostMapping(path = "/create")
    public ApiResponse<TagResponse> createTag(@RequestBody @Valid TagRequest request){
        return tagService.createTag(request).thenApply(tagResponse ->
                ApiResponse.<TagResponse>builder()
                        .message(SuccessReturnMessage.CREATE_SUCCESS.getMessage())
                        .entity(tagResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Get All Tags")
    @GetMapping(path = "/getall")
    public ApiResponse<List<TagResponse>> getAllTopics(@RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "10") int perPage){
        return tagService.getAllTags(page, perPage).thenApply(tagResponses ->
                ApiResponse.<List<TagResponse>>builder()
                        .entity(tagResponses)
                        .build()
        ).join();
    }

    @Operation(summary = "Get All Tags", description = "Get All Topics Based On Topic Name And/Or Color")
    @GetMapping(path = "/getall/by-filtering")
    public ApiResponse<List<TagResponse>> getAllTopicsByNameContaining(@RequestParam(defaultValue = "1") int page,
                                                                       @RequestParam(defaultValue = "10") int perPage,
                                                                       @RequestParam(required = false) String name,
                                                                       @RequestParam(required = false) String targetColorHex){
        return tagService.getAllTagsByFilteringNameAndColor(page, perPage, name, targetColorHex).thenApply(tagResponses ->
                ApiResponse.<List<TagResponse>>builder()
                        .entity(tagResponses)
                        .build()
        ).join();
    }

    @Operation(summary = "Get A Tag", description = "Get A Tag By ID")
    @GetMapping(path = "/get/{tagId}")
    public ApiResponse<TagResponse> getTagById(@PathVariable UUID tagId){
        return tagService.getTagById(tagId).thenApply(tagResponse ->
                ApiResponse.<TagResponse>builder()
                        .message(SuccessReturnMessage.SEARCH_SUCCESS.getMessage())
                        .entity(tagResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Delete A Tag", description = "Delete A Tag By ID")
    @DeleteMapping(path = "/delete/{tagId}")
    public ApiResponse<TagResponse> deleteTagById(@PathVariable UUID tagId){
        return tagService.deleteTagById(tagId).thenApply(tagResponse ->
                ApiResponse.<TagResponse>builder()
                        .message(SuccessReturnMessage.DELETE_SUCCESS.getMessage())
                        .entity(tagResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Update A Tag", description = "Update A Tag By ID")
    @PutMapping(path = "/update/{tagId}")
    public ApiResponse<TagResponse> updateTagById(@PathVariable UUID tagId,
                                                  @RequestBody @Valid TagRequest request){
        return tagService.updateTagById(tagId, request).thenApply(tagResponse ->
                ApiResponse.<TagResponse>builder()
                        .message(SuccessReturnMessage.UPDATE_SUCCESS.getMessage())
                        .entity(tagResponse)
                        .build()
        ).join();
    }
}
