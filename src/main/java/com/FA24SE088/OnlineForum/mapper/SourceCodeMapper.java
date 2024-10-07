package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.DocumentRequest;
import com.FA24SE088.OnlineForum.dto.response.DocumentResponse;
import com.FA24SE088.OnlineForum.entity.Document;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SourceCodeMapper {

    Document toSourceCode(DocumentRequest request);
    DocumentResponse toResponse(Document document);
}
