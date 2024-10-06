package com.FA24SE088.OnlineForum.dto.response;

import com.FA24SE088.OnlineForum.dto.request.SectionRequest;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SourceCodeResponse {
    String name;
    String image;
    double price;
    String type;
    String status;
    List<SectionResponse> sectionList;

}
