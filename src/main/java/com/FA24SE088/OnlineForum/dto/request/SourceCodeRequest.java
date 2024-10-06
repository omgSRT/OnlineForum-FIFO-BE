package com.FA24SE088.OnlineForum.dto.request;

import com.FA24SE088.OnlineForum.entity.Section;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SourceCodeRequest {
    String name;
    String image;
    double price;
    String type;
    String status;
    List<SectionRequest> sectionList;

}
