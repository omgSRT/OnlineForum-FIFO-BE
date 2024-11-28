package com.FA24SE088.OnlineForum.dto.request;

import jakarta.persistence.Column;
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
    String title;
    Date startDate;
    Date endDate;
    String location;
    @Column(columnDefinition = "MEDIUMTEXT")
    String image;
    @Column(columnDefinition = "MEDIUMTEXT")
    String content;
    String link;
//    String status;

}
