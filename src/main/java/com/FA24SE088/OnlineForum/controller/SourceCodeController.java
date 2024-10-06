package com.FA24SE088.OnlineForum.controller;


import com.FA24SE088.OnlineForum.dto.request.SourceCodeRequest;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.SourceCodeResponse;
import com.FA24SE088.OnlineForum.entity.SourceCode;
import com.FA24SE088.OnlineForum.service.SourceCodeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/source-code")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SourceCodeController {
    final SourceCodeService sourceCodeService;
    @PostMapping("/create")
    public ApiResponse<SourceCodeResponse> create(@RequestBody SourceCodeRequest request) {
        return ApiResponse.<SourceCodeResponse>builder()
                .entity(sourceCodeService.createSourceCode(request))
                .build();
    }
    @PostMapping("/create-2")
    public ApiResponse<SourceCodeResponse> create2(@RequestBody SourceCodeRequest request) {
        return ApiResponse.<SourceCodeResponse>builder()
                .entity(sourceCodeService.createDocument(request))
                .build();
    }


}
