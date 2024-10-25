package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.EventRequest;
import com.FA24SE088.OnlineForum.dto.request.RedeemRequest;
import com.FA24SE088.OnlineForum.dto.response.EventResponse;
import com.FA24SE088.OnlineForum.dto.response.RedeemResponse;
import com.FA24SE088.OnlineForum.entity.*;
import com.FA24SE088.OnlineForum.enums.EventStatus;
import com.FA24SE088.OnlineForum.enums.FeedbackStatus;
import com.FA24SE088.OnlineForum.enums.TransactionType;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.EventMapper;
import com.FA24SE088.OnlineForum.mapper.RedeemMapper;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import com.FA24SE088.OnlineForum.utils.PaginationUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class EventService {
    UnitOfWork unitOfWork;
    EventMapper eventMapper;
    PaginationUtils paginationUtils;

    public EventResponse createEvent(EventRequest eventRequest) {
        if (eventRequest.getEndDate() != null) {
            validateEventDates(eventRequest.getStartDate(), eventRequest.getEndDate());
        }
        if (!eventRequest.getStatus().equals(EventStatus.UPCOMING.name()) &&
                !eventRequest.getStatus().equals(EventStatus.ONGOING.name()) &&
                !eventRequest.getStatus().equals(EventStatus.CONCLUDED.name())) {
            throw new AppException(ErrorCode.WRONG_STATUS);
        } else {
            Event event = eventMapper.toEvent(eventRequest);
            Event savedEvent = unitOfWork.getEventRepository().save(event);
            return mapToResponse(savedEvent);
        }
    }
    public List<EventResponse> filterEvents(int page, int perPage, String title, String location, String status) {
        List<EventResponse> result;

        List<EventResponse> allEvents = unitOfWork.getEventRepository().findAll().stream()
                .map(eventMapper::toResponse)
                .toList();

        if (title == null && location == null && status == null) {
            result = allEvents.stream()
                    .sorted(Comparator.comparingInt(x -> {
                        switch (x.getStatus()) {
                            case "ONGOING": return 1;
                            case "UPCOMING": return 2;
                            case "CONCLUDED": return 3;
                            default: return 4; // Các trạng thái không xác định ở cuối
                        }
                    }))
                    .toList();
        } else {

            result = allEvents.stream()
                    .filter(x -> (title == null || (x.getTitle() != null && x.getTitle().contains(title))))
                    .filter(x -> (location == null || (x.getLocation() != null && x.getLocation().contains(location))))
                    .filter(x -> (status == null || (x.getStatus() != null && x.getStatus().equals(status))))
                    .sorted(Comparator.comparingInt(x -> {
                        switch (x.getStatus()) {
                            case "ONGOING": return 1;
                            case "UPCOMING": return 2;
                            case "CONCLUDED": return 3;
                            default: return 4;
                        }
                    }))
                    .toList();
        }

        return paginationUtils.convertListToPage(page, perPage, result);
    }


    public Optional<EventResponse> updateEvent(UUID eventId, EventRequest eventRequest) {
        Optional<Event> eventOptional = unitOfWork.getEventRepository().findById(eventId);
        validateEventDates(eventRequest.getStartDate(), eventRequest.getEndDate());
        if (!eventRequest.getStatus().equals(EventStatus.UPCOMING.name()) &&
                !eventRequest.getStatus().equals(EventStatus.ONGOING.name()) &&
                !eventRequest.getStatus().equals(EventStatus.CONCLUDED.name()))
            throw new AppException(ErrorCode.WRONG_STATUS);
        if (eventOptional.isPresent()) {
            Event event = eventOptional.get();
            event.setTitle(eventRequest.getTitle());
            event.setStartDate(eventRequest.getStartDate());
            event.setEndDate(eventRequest.getEndDate());
            event.setLocation(eventRequest.getLocation());
            event.setImage(eventRequest.getImage());
            event.setContent(eventRequest.getContent());
            event.setLink(eventRequest.getLink());
            event.setStatus(eventRequest.getStatus());

            Event updatedEvent = unitOfWork.getEventRepository().save(event);
            return Optional.of(mapToResponse(updatedEvent));
        }
        return Optional.empty();
    }

    public void deleteEvent(UUID eventId) {
        unitOfWork.getEventRepository().deleteById(eventId);
    }

    public List<EventResponse> getAllEvents() {
        List<Event> events = unitOfWork.getEventRepository().findAll();
        return events.stream().map(eventMapper::toResponse).toList();
    }

    public Optional<EventResponse> getEventById(UUID eventId) {
        Optional<Event> event = unitOfWork.getEventRepository().findById(eventId);
        return event.map(eventMapper::toResponse);
    }

    private EventResponse mapToResponse(Event event) {
        return new EventResponse(event.getEventId(), event.getTitle(), event.getStartDate(), event.getEndDate(), event.getLocation(), event.getImage(), event.getContent(), event.getLink(), event.getStatus());
    }

    private void validateEventDates(Date startDate, Date endDate) {
        if (startDate.after(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }
    }
}
