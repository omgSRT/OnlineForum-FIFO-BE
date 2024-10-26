package com.FA24SE088.OnlineForum.controller;

import com.FA24SE088.OnlineForum.dto.request.DraftCreateRequest;
import com.FA24SE088.OnlineForum.dto.request.DraftUpdateRequest;
import com.FA24SE088.OnlineForum.dto.request.PostCreateRequest;
import com.FA24SE088.OnlineForum.dto.request.PostUpdateRequest;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.PointResponse;
import com.FA24SE088.OnlineForum.dto.response.PostGetByIdResponse;
import com.FA24SE088.OnlineForum.dto.response.PostResponse;
import com.FA24SE088.OnlineForum.enums.PostStatus;
import com.FA24SE088.OnlineForum.enums.SuccessReturnMessage;
import com.FA24SE088.OnlineForum.enums.UpdatePostStatus;
import com.FA24SE088.OnlineForum.service.PostService;
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
@RequestMapping("/post")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PostController {
    PostService postService;

    @Operation(summary = "Create New Post")
    @PostMapping(path = "/create")
    public ApiResponse<PostResponse> createPost(@RequestBody @Valid PostCreateRequest request){
        return postService.createPost(request).thenApply(postResponse ->
                ApiResponse.<PostResponse>builder()
                        .message(SuccessReturnMessage.CREATE_SUCCESS.getMessage())
                        .entity(postResponse)
                        .build()
                ).join();
    }

    @Operation(summary = "Get All Posts")
    @GetMapping(path = "/getall")
    public ApiResponse<List<PostResponse>> getAllPosts(@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int perPage,
                                                        @RequestParam(required = false) UUID accountId,
                                                        @RequestParam(required = false) UUID topicId,
                                                        @RequestParam(required = false) UUID tagId,
                                                        @RequestParam(required = false) List<PostStatus> statuses,
                                                        @RequestParam(required = false) Boolean isFolloweeIncluded){
        return postService.getAllPosts(page, perPage, accountId, topicId, tagId, statuses, isFolloweeIncluded).thenApply(postResponses ->
                ApiResponse.<List<PostResponse>>builder()
                        .entity(postResponses)
                        .build()
                ).join();
    }

    @Operation(summary = "Get All Posts", description = "Get All Posts For Current User")
    @GetMapping(path = "/getall/by-current-user")
    public ApiResponse<List<PostResponse>> getAllPostsForCurrentUser(@RequestParam(defaultValue = "1") int page,
                                                                     @RequestParam(defaultValue = "10") int perPage){
        return postService.getAllPostsForCurrentUser(page, perPage).thenApply(postResponses ->
                ApiResponse.<List<PostResponse>>builder()
                        .entity(postResponses)
                        .build()
        ).join();
    }

    @Operation(summary = "Get Post", description = "Get Post By ID")
    @GetMapping(path = "/get/{postId}")
    public ApiResponse<PostResponse> getPostById(@PathVariable UUID postId){
        return postService.getPostById(postId).thenApply(postResponse ->
                ApiResponse.<PostResponse>builder()
                        .entity(postResponse)
                        .build()
                ).join();
    }

    @Operation(summary = "Update Post", description = "Update Post By ID")
    @PutMapping(path = "/update/{postId}")
    public ApiResponse<PostResponse> updatePostById(@PathVariable UUID postId,
                                                    @RequestBody @Valid PostUpdateRequest request){
        return postService.updatePostById(postId, request).thenApply(postResponse ->
                ApiResponse.<PostResponse>builder()
                        .message(SuccessReturnMessage.UPDATE_SUCCESS.getMessage())
                        .entity(postResponse)
                        .build()
                ).join();
    }

    @Operation(summary = "Delete Post", description = "Delete Post By Changing Status")
    @PutMapping(path = "/update/{postId}/status/hidden")
    public ApiResponse<PostResponse> deletePostByChangingStatusById(@PathVariable UUID postId){
        return postService.deleteByChangingPostStatusById(postId).thenApply(postResponse ->
                ApiResponse.<PostResponse>builder()
                        .message(SuccessReturnMessage.DELETE_SUCCESS.getMessage())
                        .entity(postResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Update Post", description = "Update Status Post")
    @PutMapping(path = "/update/{postId}/status")
    public ApiResponse<PostResponse> deletePostByChangingStatusById(@PathVariable UUID postId,
                                                                    @RequestParam UpdatePostStatus status){
        return postService.updatePostStatusById(postId, status).thenApply(postResponse ->
                ApiResponse.<PostResponse>builder()
                        .message(SuccessReturnMessage.UPDATE_SUCCESS.getMessage())
                        .entity(postResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Create New Draft")
    @PostMapping(path = "/create/draft")
    public ApiResponse<PostResponse> createDraft(@RequestBody @Valid DraftCreateRequest request){
        return postService.createDraft(request).thenApply(postResponse ->
                ApiResponse.<PostResponse>builder()
                        .message(SuccessReturnMessage.CREATE_SUCCESS.getMessage())
                        .entity(postResponse)
                        .build()
        ).join();
    }
    @Operation(summary = "Get All Drafts")
    @GetMapping(path = "/getall/draft")
    public ApiResponse<List<PostResponse>> getAllDrafts(@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int perPage,
                                                        @RequestParam(required = false) UUID accountId){
        return postService.getAllDrafts(page, perPage, accountId).thenApply(postResponses ->
                ApiResponse.<List<PostResponse>>builder()
                        .entity(postResponses)
                        .build()
        ).join();
    }
    @Operation(summary = "Get All Drafts", description = "Get All Drafts For Current User")
    @GetMapping(path = "/getall/draft/by-current-user")
    public ApiResponse<List<PostResponse>> getAllDraftsForCurrentUser(@RequestParam(defaultValue = "1") int page,
                                                                      @RequestParam(defaultValue = "10") int perPage){
        return postService.getAllDraftsForCurrentUser(page, perPage).thenApply(postResponses ->
                ApiResponse.<List<PostResponse>>builder()
                        .entity(postResponses)
                        .build()
        ).join();
    }
    @Operation(summary = "Update Draft", description = "Update Draft By ID")
    @PutMapping(path = "/update/draft/{draftId}")
    public ApiResponse<PostResponse> updateDraft(@PathVariable UUID draftId,
                                                 @RequestBody @Valid DraftUpdateRequest request){
        return postService.updateDraftById(draftId, request).thenApply(postResponse ->
                ApiResponse.<PostResponse>builder()
                        .message(SuccessReturnMessage.UPDATE_SUCCESS.getMessage())
                        .entity(postResponse)
                        .build()
        ).join();
    }
    @Operation(summary = "Update Draft To Completed Post", description = "Update Draft To Completed Post By ID")
    @PutMapping(path = "/update/draft/to-post/{draftId}")
    public ApiResponse<PostResponse> updateDraftToPost(@PathVariable UUID draftId){
        return postService.updateDraftToPostById(draftId).thenApply(postResponse ->
                ApiResponse.<PostResponse>builder()
                        .message(SuccessReturnMessage.UPDATE_SUCCESS.getMessage())
                        .entity(postResponse)
                        .build()
        ).join();
    }
    @Operation(summary = "Delete Drafts", description = "Delete Drafts By List Of Draft IDs")
    @DeleteMapping(path = "/delete/draft")
    public ApiResponse<List<PostResponse>> deleteDraftsById(@RequestBody @Valid List<UUID> draftIds){
        return postService.deleteDraftsById(draftIds).thenApply(postResponses ->
                ApiResponse.<List<PostResponse>>builder()
                        .message(SuccessReturnMessage.DELETE_SUCCESS.getMessage())
                        .entity(postResponses)
                        .build()
        ).join();
    }
}
