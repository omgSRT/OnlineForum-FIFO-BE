package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.TopicRequest;
import com.FA24SE088.OnlineForum.dto.request.TopicUpdateRequest;
import com.FA24SE088.OnlineForum.dto.response.TopicNoCategoryResponse;
import com.FA24SE088.OnlineForum.dto.response.TopicResponse;
import com.FA24SE088.OnlineForum.entity.Topic;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-08T21:49:58+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.7 (Oracle Corporation)"
)
@Component
public class TopicMapperImpl implements TopicMapper {

    @Override
    public Topic toTopic(TopicRequest request) {
        if ( request == null ) {
            return null;
        }

        Topic.TopicBuilder topic = Topic.builder();

        topic.name( request.getName() );

        return topic.build();
    }

    @Override
    public TopicResponse toTopicResponse(Topic topic) {
        if ( topic == null ) {
            return null;
        }

        TopicResponse.TopicResponseBuilder topicResponse = TopicResponse.builder();

        topicResponse.topicId( topic.getTopicId() );
        topicResponse.name( topic.getName() );
        topicResponse.category( topic.getCategory() );

        return topicResponse.build();
    }

    @Override
    public TopicNoCategoryResponse toTopicNoCategoryResponse(Topic topic) {
        if ( topic == null ) {
            return null;
        }

        TopicNoCategoryResponse.TopicNoCategoryResponseBuilder topicNoCategoryResponse = TopicNoCategoryResponse.builder();

        topicNoCategoryResponse.topicId( topic.getTopicId() );
        topicNoCategoryResponse.name( topic.getName() );

        return topicNoCategoryResponse.build();
    }

    @Override
    public void updateTopic(Topic topic, TopicUpdateRequest request) {
        if ( request == null ) {
            return;
        }

        topic.setName( request.getName() );
    }
}
