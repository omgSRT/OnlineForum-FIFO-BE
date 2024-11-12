package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.PostFileRequest;
import com.FA24SE088.OnlineForum.dto.response.PostFileResponse;
import com.FA24SE088.OnlineForum.entity.PostFile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PostFileMapper {
    PostFile toPostFile(PostFileRequest request);

    PostFileResponse toPostFileResponse(PostFile postFile);
}
