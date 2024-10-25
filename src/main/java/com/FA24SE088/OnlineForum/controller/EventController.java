package com.FA24SE088.OnlineForum.controller;


import com.FA24SE088.OnlineForum.dto.request.AccountRequest;
import com.FA24SE088.OnlineForum.dto.request.AccountUpdateCategoryRequest;
import com.FA24SE088.OnlineForum.dto.request.EventRequest;
import com.FA24SE088.OnlineForum.dto.response.AccountResponse;
import com.FA24SE088.OnlineForum.dto.response.ApiResponse;
import com.FA24SE088.OnlineForum.dto.response.EventResponse;
import com.FA24SE088.OnlineForum.entity.Account;
import com.FA24SE088.OnlineForum.enums.SuccessReturnMessage;
import com.FA24SE088.OnlineForum.service.AccountService;
import com.FA24SE088.OnlineForum.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/event")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EventController {
    EventService eventService;

    @Operation(summary = "Create Event", description = "Status: \n" +
            "UPCOMING,\n" +
            "    ONGOING,\n" +
            "    CONCLUDED")
    @PostMapping("/create-event")
    public ApiResponse<EventResponse> createEvent(@RequestBody EventRequest eventRequest) {
        return ApiResponse.<EventResponse>builder()
                .entity(eventService.createEvent(eventRequest))
                .build();
    }

    @Operation(summary = "Update Event", description = "Status: \n" +
            "UPCOMING,\n" +
            "    ONGOING,\n" +
            "    CONCLUDED")
    @PutMapping("/update/{eventId}")
    public ApiResponse<Optional<EventResponse>> updateEvent(@PathVariable UUID eventId, @RequestBody EventRequest eventRequest) {
        return ApiResponse.<Optional<EventResponse>>builder()
                .entity(eventService.updateEvent(eventId,eventRequest))
                .build();
    }

    @DeleteMapping("/delete/{eventId}")
    public ApiResponse<Void> deleteEvent(@PathVariable UUID eventId) {
        eventService.deleteEvent(eventId);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/get-all")
    public ApiResponse<List<EventResponse>> getAllEvents() {
        return ApiResponse.<List<EventResponse>>builder()
                .entity(eventService.getAllEvents())
                .build();
    }

    @GetMapping("/get-by-id/{eventId}")
    public ApiResponse<Optional<EventResponse>> getEventById(@PathVariable UUID eventId) {
        return ApiResponse.<Optional<EventResponse>>builder()
                .entity(eventService.getEventById(eventId))
                .build();
    }
}
