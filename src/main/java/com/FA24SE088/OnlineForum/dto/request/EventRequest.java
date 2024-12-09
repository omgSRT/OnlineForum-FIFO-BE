package com.FA24SE088.OnlineForum.dto.request;

import jakarta.persistence.Column;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventRequest {
    String title;
    Date startDate;
    Date endDate;
    String location;
    @Column(columnDefinition = "MEDIUMTEXT")
    String image;
    @Column(columnDefinition = "MEDIUMTEXT")
    String content;
    String link;
}
