package com.FA24SE088.OnlineForum.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RewardResponse {
    UUID rewardId;
    String name;
    String image;
    double price;
    String type;
    String status;
    List<SectionResponse> sectionList;
}
