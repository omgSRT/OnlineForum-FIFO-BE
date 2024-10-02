package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.CategoryUpdateRequest;
import com.FA24SE088.OnlineForum.dto.request.TopicRequest;
import com.FA24SE088.OnlineForum.dto.request.TopicUpdateRequest;
import com.FA24SE088.OnlineForum.dto.response.TopicNoCategoryResponse;
import com.FA24SE088.OnlineForum.dto.response.TopicResponse;
import com.FA24SE088.OnlineForum.entity.Category;
import com.FA24SE088.OnlineForum.entity.Topic;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TopicMapper {
    Topic toTopic(TopicRequest request);

    TopicResponse toTopicResponse(Topic topic);

    TopicNoCategoryResponse toTopicNoCategoryResponse(Topic topic);

    @Mapping(target = "category", ignore = true)
    void updateTopic(@MappingTarget Topic topic, TopicUpdateRequest request);
}
