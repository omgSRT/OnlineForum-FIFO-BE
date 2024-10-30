package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.RewardRequest;
import com.FA24SE088.OnlineForum.dto.response.*;
import com.FA24SE088.OnlineForum.entity.Reward;
import com.FA24SE088.OnlineForum.entity.ContentSection;
import com.FA24SE088.OnlineForum.entity.Section;
import com.FA24SE088.OnlineForum.entity.Media;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RewardMapper {

    Reward toReward(RewardRequest request);

    @Mapping(target = "rewardId",source = "rewardId")
    RewardResponse toResponse(Reward reward);


    // Ánh xạ từ Section sang SectionResponse
    @Mapping(target = "contentSectionResponses", source = "contentSectionList")
    SectionResponse toSectionResponse(Section section);
    ContentSectionResponse toContentSectionResponse(ContentSection contentSection);

    // Ánh xạ từ ImageSection sang ImageSectionResponse
    MediaResponse toImageSectionResponse(Media media);

    // Ánh xạ từ VideoSection sang VideoSectionResponse
    VideoSectionResponse toVideoSectionResponse(Media media);


    List<SectionResponse> mapSections(List<Section> sections);

    void updateRewardFromRequest (@MappingTarget Reward reward, RewardRequest request);


}
