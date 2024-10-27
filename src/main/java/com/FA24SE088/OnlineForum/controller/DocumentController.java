package com.FA24SE088.OnlineForum.controller;


import com.FA24SE088.OnlineForum.dto.request.DocumentRequest;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.DocumentResponse;
import com.FA24SE088.OnlineForum.service.DocumentService;
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
@RequestMapping("/document")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DocumentController {
    DocumentService documentService;

    @Operation(summary = "Create Document", description = "Status: \n" +
            "ACTIVE,\n" +
            "    INACTIVE")
    @PostMapping("/create")
    public ApiResponse<DocumentResponse> create(@RequestBody DocumentRequest request) {
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

    @Operation(summary = "Update Document", description = "Status: \n" +
            "ACTIVE,\n" +
            "    INACTIVE")
    @PutMapping("/update/{id}")
    public ApiResponse<DocumentResponse> update(@PathVariable UUID id, @RequestBody DocumentRequest request) {
        return ApiResponse.<DocumentResponse>builder()
                .entity(documentService.update(id, request))
                .build();
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<DocumentResponse> update(@PathVariable UUID id) {
        documentService.deleteDocument(id);
        return ApiResponse.<DocumentResponse>builder()
                .build();
    }


}
