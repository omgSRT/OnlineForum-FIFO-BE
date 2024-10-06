package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.SourceCodeRequest;
import com.FA24SE088.OnlineForum.dto.response.SourceCodeResponse;
import com.FA24SE088.OnlineForum.entity.SourceCode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SourceCodeMapper {

    SourceCode toSourceCode(SourceCodeRequest request);
    SourceCodeResponse toResponse(SourceCode sourceCode);
}
