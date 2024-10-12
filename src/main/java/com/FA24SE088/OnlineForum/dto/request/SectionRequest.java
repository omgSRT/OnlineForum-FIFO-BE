package com.FA24SE088.OnlineForum.dto.request;

import com.FA24SE088.OnlineForum.entity.Section;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SectionRequest {
    String linkGit; // Chỉ cần thông tin cần thiết
    String content;
    List<ImageSectionRequest> imageSectionList;
    List<VideoSectionRequest> videoSectionList;
}

