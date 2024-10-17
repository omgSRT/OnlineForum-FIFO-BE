package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.EventRequest;
import com.FA24SE088.OnlineForum.dto.response.EventResponse;
import com.FA24SE088.OnlineForum.entity.Event;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface EventMapper {
    Event toEvent(EventRequest request);

    EventResponse toResponse(Event event);
}
