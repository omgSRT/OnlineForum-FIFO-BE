package com.FA24SE088.OnlineForum.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentRequest {
    String name;
    String image;
    double price;
    String type;
    String status;
    List<SectionRequest> sectionList;

}
