package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.TagRequest;
import com.FA24SE088.OnlineForum.dto.response.TagResponse;
import com.FA24SE088.OnlineForum.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TagMapper {
    Tag toTag(TagRequest request);

    TagResponse toTagResponse(Tag tag);

    @Mapping(target = "postList", ignore = true)
    void updateTag(@MappingTarget Tag tag, TagRequest request);
}
