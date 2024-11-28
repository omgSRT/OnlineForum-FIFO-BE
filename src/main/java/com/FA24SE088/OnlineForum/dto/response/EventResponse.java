package com.FA24SE088.OnlineForum.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventResponse {
    private UUID eventId;
    private String title;
    private Date startDate;
    private Date endDate;
    private String location;
    private String image;
    private String content;
    private String link;
    private String status;
}
