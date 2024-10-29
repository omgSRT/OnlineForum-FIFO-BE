package com.FA24SE088.OnlineForum.dto.response;

import com.FA24SE088.OnlineForum.dto.request.MediaRequest;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContentSectionResponse {
    String content;
    String code;
    Integer number;
    List<MediaResponse> mediaList;
}
