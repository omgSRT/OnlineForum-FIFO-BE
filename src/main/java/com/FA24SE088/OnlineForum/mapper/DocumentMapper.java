package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.DocumentRequest;
import com.FA24SE088.OnlineForum.dto.response.DocumentResponse;
import com.FA24SE088.OnlineForum.dto.response.ImageSectionResponse;
import com.FA24SE088.OnlineForum.dto.response.SectionResponse;
import com.FA24SE088.OnlineForum.dto.response.VideoSectionResponse;
import com.FA24SE088.OnlineForum.entity.Document;
import com.FA24SE088.OnlineForum.entity.ImageSection;
import com.FA24SE088.OnlineForum.entity.Section;
import com.FA24SE088.OnlineForum.entity.VideoSection;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    Document toSourceCode(DocumentRequest request);

    DocumentResponse toResponse(Document document);


    // Ánh xạ từ Section sang SectionResponse
    SectionResponse toSectionResponse(Section section);

    // Ánh xạ từ ImageSection sang ImageSectionResponse
    ImageSectionResponse toImageSectionResponse(ImageSection imageSection);

    // Ánh xạ từ VideoSection sang VideoSectionResponse
    VideoSectionResponse toVideoSectionResponse(VideoSection videoSection);


    List<SectionResponse> mapSections(List<Section> sections);


}
