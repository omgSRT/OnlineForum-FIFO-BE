package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.RewardRequest;
import com.FA24SE088.OnlineForum.dto.response.DocumentResponse;
import com.FA24SE088.OnlineForum.dto.response.ImageSectionResponse;
import com.FA24SE088.OnlineForum.dto.response.SectionResponse;
import com.FA24SE088.OnlineForum.dto.response.VideoSectionResponse;
import com.FA24SE088.OnlineForum.entity.Reward;
import com.FA24SE088.OnlineForum.entity.ContentSection;
import com.FA24SE088.OnlineForum.entity.Section;
import com.FA24SE088.OnlineForum.entity.Media;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    Reward toDocument(RewardRequest request);

    @Mapping(target = "documentId",source = "documentId")
    DocumentResponse toResponse(Reward reward);


    // Ánh xạ từ Section sang SectionResponse
    SectionResponse toSectionResponse(Section section);

    // Ánh xạ từ ImageSection sang ImageSectionResponse
    ImageSectionResponse toImageSectionResponse(ContentSection contentSection);

    // Ánh xạ từ VideoSection sang VideoSectionResponse
    VideoSectionResponse toVideoSectionResponse(Media media);


    List<SectionResponse> mapSections(List<Section> sections);

    void updateDocumentFromRequest (@MappingTarget Reward reward, RewardRequest request);


}
