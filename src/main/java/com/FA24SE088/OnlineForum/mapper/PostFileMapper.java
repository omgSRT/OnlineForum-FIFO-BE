package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.PostFileCreateRequest;
import com.FA24SE088.OnlineForum.dto.request.PostFileRequest;
import com.FA24SE088.OnlineForum.dto.request.PostFileUpdateRequest;
import com.FA24SE088.OnlineForum.dto.response.PostFileResponse;
import com.FA24SE088.OnlineForum.entity.PostFile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PostFileMapper {
    PostFile toPostFile(PostFileRequest request);

    PostFile toPostFile(PostFileCreateRequest request);

    @Mapping(target = "post", ignore = true)
    void updatePostFile(@MappingTarget PostFile postFile, PostFileUpdateRequest request);

    PostFileResponse toPostFileResponse(PostFile postFile);
}
