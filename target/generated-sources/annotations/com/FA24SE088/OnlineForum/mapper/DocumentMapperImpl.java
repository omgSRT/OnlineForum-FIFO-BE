package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.DocumentRequest;
import com.FA24SE088.OnlineForum.dto.request.ImageSectionRequest;
import com.FA24SE088.OnlineForum.dto.request.SectionRequest;
import com.FA24SE088.OnlineForum.dto.request.VideoSectionRequest;
import com.FA24SE088.OnlineForum.dto.response.DocumentResponse;
import com.FA24SE088.OnlineForum.dto.response.ImageSectionResponse;
import com.FA24SE088.OnlineForum.dto.response.SectionResponse;
import com.FA24SE088.OnlineForum.dto.response.VideoSectionResponse;
import com.FA24SE088.OnlineForum.entity.Document;
import com.FA24SE088.OnlineForum.entity.ImageSection;
import com.FA24SE088.OnlineForum.entity.Section;
import com.FA24SE088.OnlineForum.entity.VideoSection;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-18T12:42:12+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class DocumentMapperImpl implements DocumentMapper {

    @Override
    public Document toSourceCode(DocumentRequest request) {
        if ( request == null ) {
            return null;
        }

        Document.DocumentBuilder document = Document.builder();

        document.name( request.getName() );
        document.image( request.getImage() );
        document.price( request.getPrice() );
        document.type( request.getType() );
        document.status( request.getStatus() );
        document.sectionList( sectionRequestListToSectionList( request.getSectionList() ) );

        return document.build();
    }

    @Override
    public DocumentResponse toResponse(Document document) {
        if ( document == null ) {
            return null;
        }

        DocumentResponse.DocumentResponseBuilder documentResponse = DocumentResponse.builder();

        documentResponse.name( document.getName() );
        documentResponse.image( document.getImage() );
        documentResponse.price( document.getPrice() );
        documentResponse.type( document.getType() );
        documentResponse.status( document.getStatus() );
        documentResponse.sectionList( mapSections( document.getSectionList() ) );

        return documentResponse.build();
    }

    @Override
    public SectionResponse toSectionResponse(Section section) {
        if ( section == null ) {
            return null;
        }

        SectionResponse.SectionResponseBuilder sectionResponse = SectionResponse.builder();

        sectionResponse.linkGit( section.getLinkGit() );
        sectionResponse.imageSectionList( imageSectionListToImageSectionResponseList( section.getImageSectionList() ) );
        sectionResponse.videoSectionList( videoSectionListToVideoSectionResponseList( section.getVideoSectionList() ) );

        return sectionResponse.build();
    }

    @Override
    public ImageSectionResponse toImageSectionResponse(ImageSection imageSection) {
        if ( imageSection == null ) {
            return null;
        }

        ImageSectionResponse.ImageSectionResponseBuilder imageSectionResponse = ImageSectionResponse.builder();

        imageSectionResponse.url( imageSection.getUrl() );

        return imageSectionResponse.build();
    }

    @Override
    public VideoSectionResponse toVideoSectionResponse(VideoSection videoSection) {
        if ( videoSection == null ) {
            return null;
        }

        VideoSectionResponse.VideoSectionResponseBuilder videoSectionResponse = VideoSectionResponse.builder();

        videoSectionResponse.url( videoSection.getUrl() );

        return videoSectionResponse.build();
    }

    @Override
    public List<SectionResponse> mapSections(List<Section> sections) {
        if ( sections == null ) {
            return null;
        }

        List<SectionResponse> list = new ArrayList<SectionResponse>( sections.size() );
        for ( Section section : sections ) {
            list.add( toSectionResponse( section ) );
        }

        return list;
    }

    protected ImageSection imageSectionRequestToImageSection(ImageSectionRequest imageSectionRequest) {
        if ( imageSectionRequest == null ) {
            return null;
        }

        ImageSection.ImageSectionBuilder imageSection = ImageSection.builder();

        imageSection.url( imageSectionRequest.getUrl() );

        return imageSection.build();
    }

    protected List<ImageSection> imageSectionRequestListToImageSectionList(List<ImageSectionRequest> list) {
        if ( list == null ) {
            return null;
        }

        List<ImageSection> list1 = new ArrayList<ImageSection>( list.size() );
        for ( ImageSectionRequest imageSectionRequest : list ) {
            list1.add( imageSectionRequestToImageSection( imageSectionRequest ) );
        }

        return list1;
    }

    protected VideoSection videoSectionRequestToVideoSection(VideoSectionRequest videoSectionRequest) {
        if ( videoSectionRequest == null ) {
            return null;
        }

        VideoSection.VideoSectionBuilder videoSection = VideoSection.builder();

        videoSection.url( videoSectionRequest.getUrl() );

        return videoSection.build();
    }

    protected List<VideoSection> videoSectionRequestListToVideoSectionList(List<VideoSectionRequest> list) {
        if ( list == null ) {
            return null;
        }

        List<VideoSection> list1 = new ArrayList<VideoSection>( list.size() );
        for ( VideoSectionRequest videoSectionRequest : list ) {
            list1.add( videoSectionRequestToVideoSection( videoSectionRequest ) );
        }

        return list1;
    }

    protected Section sectionRequestToSection(SectionRequest sectionRequest) {
        if ( sectionRequest == null ) {
            return null;
        }

        Section.SectionBuilder section = Section.builder();

        section.linkGit( sectionRequest.getLinkGit() );
        section.content( sectionRequest.getContent() );
        section.imageSectionList( imageSectionRequestListToImageSectionList( sectionRequest.getImageSectionList() ) );
        section.videoSectionList( videoSectionRequestListToVideoSectionList( sectionRequest.getVideoSectionList() ) );

        return section.build();
    }

    protected List<Section> sectionRequestListToSectionList(List<SectionRequest> list) {
        if ( list == null ) {
            return null;
        }

        List<Section> list1 = new ArrayList<Section>( list.size() );
        for ( SectionRequest sectionRequest : list ) {
            list1.add( sectionRequestToSection( sectionRequest ) );
        }

        return list1;
    }

    protected List<ImageSectionResponse> imageSectionListToImageSectionResponseList(List<ImageSection> list) {
        if ( list == null ) {
            return null;
        }

        List<ImageSectionResponse> list1 = new ArrayList<ImageSectionResponse>( list.size() );
        for ( ImageSection imageSection : list ) {
            list1.add( toImageSectionResponse( imageSection ) );
        }

        return list1;
    }

    protected List<VideoSectionResponse> videoSectionListToVideoSectionResponseList(List<VideoSection> list) {
        if ( list == null ) {
            return null;
        }

        List<VideoSectionResponse> list1 = new ArrayList<VideoSectionResponse>( list.size() );
        for ( VideoSection videoSection : list ) {
            list1.add( toVideoSectionResponse( videoSection ) );
        }

        return list1;
    }
}
