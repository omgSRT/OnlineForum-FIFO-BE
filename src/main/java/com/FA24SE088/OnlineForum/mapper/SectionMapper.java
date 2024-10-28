package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.ContentSectionRequest;
import com.FA24SE088.OnlineForum.dto.request.SectionRequest;
import com.FA24SE088.OnlineForum.entity.Section;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SectionMapper {

//    @Mapping(target = "contentSectionList", ignore = true)
////    @Mapping(target = "videoSectionList", ignore = true)
//    Section toSection(ContentSectionRequest request);

    @Mapping(target = "contentSectionList", ignore = true)
//    @Mapping(target = "videoSectionList", ignore = true)
    Section toSection(SectionRequest request);

}
