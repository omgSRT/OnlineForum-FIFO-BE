package com.FA24SE088.OnlineForum.controller;


import com.FA24SE088.OnlineForum.dto.request.DocumentRequest;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.DocumentResponse;
import com.FA24SE088.OnlineForum.entity.Document;
import com.FA24SE088.OnlineForum.service.DocumentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/document")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DocumentController {
    final DocumentService documentService;

    @PostMapping("/create")
    public ApiResponse<DocumentResponse> create2(@RequestBody DocumentRequest request) {
        return ApiResponse.<DocumentResponse>builder()
                .entity(documentService.createDocument(request))
                .build();
    }

    @GetMapping("/getAll")
    public ApiResponse<List<DocumentResponse>> getAll(){
        return ApiResponse.<List<DocumentResponse>>builder()
                .entity(documentService.getAll())
                .build();
    }


}
