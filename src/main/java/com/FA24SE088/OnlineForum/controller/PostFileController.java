package com.FA24SE088.OnlineForum.controller;

import com.FA24SE088.OnlineForum.dto.request.PostFileCreateRequest;
import com.FA24SE088.OnlineForum.dto.request.PostFileUpdateRequest;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.PostFileResponse;
import com.FA24SE088.OnlineForum.enums.SuccessReturnMessage;
import com.FA24SE088.OnlineForum.service.PostFileService;
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
@RequestMapping("/post-file")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PostFileController {
    PostFileService postFileService;

    @Operation(summary = "Create New Post File")
    @PostMapping(path = "/create")
    public ApiResponse<PostFileResponse> createPostFile(@RequestBody @Valid PostFileCreateRequest request) {
        return postFileService.createPostFile(request).thenApply(postFileResponse ->
                ApiResponse.<PostFileResponse>builder()
                        .message(SuccessReturnMessage.CREATE_SUCCESS.getMessage())
                        .entity(postFileResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Delete Post File By ID")
    @DeleteMapping(path = "/delete/{postFileId}")
    public ApiResponse<PostFileResponse> deletePostFileById(@PathVariable UUID postFileId) {
        return postFileService.deletePostFile(postFileId).thenApply(postFileResponse ->
                ApiResponse.<PostFileResponse>builder()
                        .message(SuccessReturnMessage.DELETE_SUCCESS.getMessage())
                        .entity(postFileResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Update Post File By ID")
    @PutMapping(path = "/update/{postFileId}")
    public ApiResponse<PostFileResponse> updatePostFileById(@PathVariable UUID postFileId,
                                                            @RequestBody @Valid PostFileUpdateRequest request) {
        return postFileService.updatePostFile(postFileId, request).thenApply(postFileResponse ->
                ApiResponse.<PostFileResponse>builder()
                        .message(SuccessReturnMessage.UPDATE_SUCCESS.getMessage())
                        .entity(postFileResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Get All Post Files")
    @GetMapping(path = "/getall")
    public ApiResponse<List<PostFileResponse>> getAllPostFiles(@RequestParam(defaultValue = "1") int page,
                                                               @RequestParam(defaultValue = "10") int perPage,
                                                               @RequestParam(required = false) UUID postId) {
        return postFileService.getAllPostLists(page, perPage, postId).thenApply(postFileResponses ->
                ApiResponse.<List<PostFileResponse>>builder()
                        .entity(postFileResponses)
                        .build()
        ).join();
    }

    @Operation(summary = "Get Post File By ID")
    @GetMapping(path = "/get/{postFileId}")
    public ApiResponse<PostFileResponse> getPostFileById(@PathVariable UUID postFileId) {
        return postFileService.getPostFileById(postFileId).thenApply(postFileResponse ->
                ApiResponse.<PostFileResponse>builder()
                        .entity(postFileResponse)
                        .build()
        ).join();
    }
}
