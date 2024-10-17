package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.EventRequest;
import com.FA24SE088.OnlineForum.dto.response.EventResponse;
import com.FA24SE088.OnlineForum.entity.Event;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-17T23:13:10+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class EventMapperImpl implements EventMapper {

    @Override
    public Event toEvent(EventRequest request) {
        if ( request == null ) {
            return null;
        }

        Event.EventBuilder event = Event.builder();

        event.title( request.getTitle() );
        event.startDate( request.getStartDate() );
        event.endDate( request.getEndDate() );
        event.location( request.getLocation() );
        event.image( request.getImage() );
        event.content( request.getContent() );
        event.link( request.getLink() );
        event.status( request.getStatus() );

        return event.build();
    }

    @Override
    public EventResponse toResponse(Event event) {
        if ( event == null ) {
            return null;
        }

        EventResponse.EventResponseBuilder eventResponse = EventResponse.builder();

        eventResponse.eventId( event.getEventId() );
        eventResponse.title( event.getTitle() );
        eventResponse.startDate( event.getStartDate() );
        eventResponse.endDate( event.getEndDate() );
        eventResponse.location( event.getLocation() );
        eventResponse.image( event.getImage() );
        eventResponse.content( event.getContent() );
        eventResponse.link( event.getLink() );
        eventResponse.status( event.getStatus() );

        return eventResponse.build();
    }
}
