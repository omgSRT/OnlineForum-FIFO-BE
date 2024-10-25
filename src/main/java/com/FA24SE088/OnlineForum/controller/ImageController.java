package com.FA24SE088.OnlineForum.controller;

import com.FA24SE088.OnlineForum.dto.request.ImageCreateRequest;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.ImageResponse;
import com.FA24SE088.OnlineForum.enums.SuccessReturnMessage;
import com.FA24SE088.OnlineForum.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/image")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ImageController {
    ImageService imageService;

    @Operation(summary = "Add New Images To Post")
    @PostMapping(path = "/create")
    public ApiResponse<List<ImageResponse>> createImages(ImageCreateRequest request){
        return imageService.createImage(request).thenApply(imageResponses ->
                ApiResponse.<List<ImageResponse>>builder()
                        .message(SuccessReturnMessage.CREATE_SUCCESS.getMessage())
                        .entity(imageResponses)
                        .build()
                ).join();
    }

    @Operation(summary = "Get All Images")
    @GetMapping(path = "/getall")
    public ApiResponse<List<ImageResponse>> getImages(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int perPage,
                                                      @RequestParam(required = false) UUID postId){
        return imageService.getAllImages(page, perPage, postId).thenApply(imageResponses ->
                ApiResponse.<List<ImageResponse>>builder()
                        .entity(imageResponses)
                        .build()
        ).join();
    }

    @Operation(summary = "Get All Images")
    @GetMapping(path = "/getall/by-current-account")
    public ApiResponse<List<ImageResponse>> getImagesByAccount(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int perPage){
        return imageService.getAllImagesByAccount(page, perPage).thenApply(imageResponses ->
                ApiResponse.<List<ImageResponse>>builder()
                        .entity(imageResponses)
                        .build()
        ).join();
    }

    @Operation(summary = "Get Image", description = "Get Image By ID")
    @GetMapping(path = "/get/{imageId}")
    public ApiResponse<ImageResponse> getImageById(@PathVariable UUID imageId){
        return imageService.getImageById(imageId).thenApply(imageResponse ->
                ApiResponse.<ImageResponse>builder()
                        .entity(imageResponse)
                        .build()
        ).join();
    }

    @Operation(summary = "Delete Image", description = "Delete Image By ID")
    @DeleteMapping(path = "/delete/{postId}")
    public ApiResponse<ImageResponse> deleteImageById(@PathVariable UUID imageId){
        return imageService.deleteImageById(imageId).thenApply(imageResponse ->
                ApiResponse.<ImageResponse>builder()
                        .message(SuccessReturnMessage.DELETE_SUCCESS.getMessage())
                        .entity(imageResponse)
                        .build()
        ).join();
    }
}
