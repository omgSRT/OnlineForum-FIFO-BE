package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.ImageSectionRequest;
import com.FA24SE088.OnlineForum.dto.request.SectionRequest;
import com.FA24SE088.OnlineForum.dto.request.SourceCodeRequest;
import com.FA24SE088.OnlineForum.dto.request.VideoSectionRequest;
import com.FA24SE088.OnlineForum.dto.response.ImageSectionResponse;
import com.FA24SE088.OnlineForum.dto.response.SectionResponse;
import com.FA24SE088.OnlineForum.dto.response.SourceCodeResponse;
import com.FA24SE088.OnlineForum.dto.response.VideoSectionResponse;
import com.FA24SE088.OnlineForum.entity.ImageSection;
import com.FA24SE088.OnlineForum.entity.Section;
import com.FA24SE088.OnlineForum.entity.SourceCode;
import com.FA24SE088.OnlineForum.entity.VideoSection;
import com.FA24SE088.OnlineForum.repository.Repository.ImageSectionRepository;
import com.FA24SE088.OnlineForum.repository.Repository.SectionRepository;
import com.FA24SE088.OnlineForum.repository.Repository.SourceCodeRepository;
import com.FA24SE088.OnlineForum.repository.Repository.VideoSectionRepository;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@Service
public class SourceCodeService {

    @Autowired
    private SourceCodeRepository sourceCodeRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private ImageSectionRepository imageSectionRepository;

    @Autowired
    private VideoSectionRepository videoSectionRepository;

    @Transactional
    public SourceCodeResponse createSourceCode(SourceCodeRequest request) {
        // Bước 1: Tạo đối tượng SourceCode từ request
        SourceCode sourceCode = new SourceCode();
        sourceCode.setName(request.getName());
        sourceCode.setImage(request.getImage());
        sourceCode.setPrice(request.getPrice());
        sourceCode.setType(request.getType());
        sourceCode.setStatus(request.getStatus());

        // Lưu SourceCode vào database trước
        sourceCode = sourceCodeRepository.save(sourceCode);

        List<SectionResponse> sectionResponses = new ArrayList<>();

        // Bước 2: Xử lý tạo Section cùng các đối tượng liên quan
        for (SectionRequest sectionRequest : request.getSectionList()) {
            Section section = new Section();
            section.setCreatedDate(new Date()); // Đặt ngày hiện tại
            section.setLinkGit(sectionRequest.getLinkGit());
            section.setSourceCode(sourceCode); // Liên kết với SourceCode vừa tạo

            // Lưu Section vào database
            section = sectionRepository.save(section);

            List<ImageSectionResponse> imageResponses = new ArrayList<>();
            for (ImageSectionRequest imageRequest : sectionRequest.getImageSectionList()) {
                ImageSection imageSection = new ImageSection();
                imageSection.setUrl(imageRequest.getUrl());
                imageSection.setSection(section);

                // Lưu ImageSection vào database
                imageSection = imageSectionRepository.save(imageSection);

                // Tạo phản hồi ImageSectionResponse
                imageResponses.add(new ImageSectionResponse(imageSection.getUrl()));
            }

            List<VideoSectionResponse> videoResponses = new ArrayList<>();
            for (VideoSectionRequest videoRequest : sectionRequest.getVideoSectionList()) {
                VideoSection videoSection = new VideoSection();
                videoSection.setUrl(videoRequest.getUrl());
                videoSection.setSection(section);

                // Lưu VideoSection vào database
                videoSection = videoSectionRepository.save(videoSection);

                // Tạo phản hồi VideoSectionResponse
                videoResponses.add(new VideoSectionResponse(videoSection.getUrl()));
            }

            // Tạo phản hồi SectionResponse
            SectionResponse sectionResponse = new SectionResponse();
            sectionResponse.setLinkGit(section.getLinkGit());
            sectionResponse.setImageSectionList(imageResponses);
            sectionResponse.setVideoSectionList(videoResponses);

            sectionResponses.add(sectionResponse);
        }

        // Tạo phản hồi SourceCodeResponse
        SourceCodeResponse response = new SourceCodeResponse();
        response.setName(sourceCode.getName());
        response.setImage(sourceCode.getImage());
        response.setPrice(sourceCode.getPrice());
        response.setType(sourceCode.getType());
        response.setStatus(sourceCode.getStatus());
        response.setSectionList(sectionResponses);

        // Trả về đối tượng SourceCodeResponse
        return response;
    }
}

