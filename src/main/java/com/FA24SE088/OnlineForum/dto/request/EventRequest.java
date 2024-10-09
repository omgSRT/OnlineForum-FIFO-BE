package com.FA24SE088.OnlineForum.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventRequest {
    private String title;
    private Date startDate;
    private Date endDate;
    private String image;
    private String content;
    private String link;
}
