package com.FA24SE088.OnlineForum.dto.request;

import com.FA24SE088.OnlineForum.dto.response.SectionResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RewardRequest {
    String name;
    String image;
    double price;
    String type;
    String status;
    List<SectionRequest> sectionList;
}
