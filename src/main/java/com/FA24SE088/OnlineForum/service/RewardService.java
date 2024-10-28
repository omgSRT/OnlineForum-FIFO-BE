package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.entity.*;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.SectionMapper;
import com.FA24SE088.OnlineForum.mapper.DocumentMapper;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class DocumentService {
    UnitOfWork unitOfWork;
    DocumentMapper documentMapper;
    SectionMapper sectionMapper;

//    @Transactional
//    public DocumentResponse createSourceCode(DocumentRequest request) {
//        // Bước 1: Tạo đối tượng SourceCode từ request
//        Reward reward = new Reward();
//        reward.setName(request.getName());
//        reward.setImage(request.getImage());
//        reward.setPrice(request.getPrice());
//        reward.setType(request.getType());
//        reward.setStatus(request.getStatus());
//
//        // Lưu SourceCode vào database trước
//        reward = unitOfWork.getDocumentRepository().save(reward);
//
//        List<SectionResponse> sectionResponses = new ArrayList<>();
//
//        // Bước 2: Xử lý tạo Section cùng các đối tượng liên quan
//        for (SectionRequest sectionRequest : request.getSectionList()) {
//            Section section = new Section();
//            section.setCreatedDate(new Date()); // Đặt ngày hiện tại
//            section.setLinkGit(sectionRequest.getLinkGit());
//            section.setReward(reward); // Liên kết với SourceCode vừa tạo
//
//            // Lưu Section vào database
//            section = unitOfWork.getSectionRepository().save(section);
//
//            List<ImageSectionResponse> imageResponses = new ArrayList<>();
//            for (ImageSectionRequest imageRequest : sectionRequest.getImageSectionList()) {
//                ContentSection contentSection = new ContentSection();
//                contentSection.setUrl(imageRequest.getUrl());
//                contentSection.setSection(section);
//
//                // Lưu ImageSection vào database
//                contentSection = unitOfWork.getImageSectionRepository().save(contentSection);
//
//                // Tạo phản hồi ImageSectionResponse
//                imageResponses.add(new ImageSectionResponse(contentSection.getUrl()));
//            }
//
//            List<VideoSectionResponse> videoResponses = new ArrayList<>();
//            for (VideoSectionRequest videoRequest : sectionRequest.getVideoSectionList()) {
//                VideoSection videoSection = new VideoSection();
//                videoSection.setUrl(videoRequest.getUrl());
//                videoSection.setSection(section);
//
//                // Lưu VideoSection vào database
//                videoSection = unitOfWork.getVideoSectionRepository().save(videoSection);
//
//                // Tạo phản hồi VideoSectionResponse
//                videoResponses.add(new VideoSectionResponse(videoSection.getUrl()));
//            }
//
//            // Tạo phản hồi SectionResponse
//            SectionResponse sectionResponse = new SectionResponse();
//            sectionResponse.setLinkGit(section.getLinkGit());
//            sectionResponse.setImageSectionList(imageResponses);
//            sectionResponse.setVideoSectionList(videoResponses);
//
//            sectionResponses.add(sectionResponse);
//        }
//
//        // Tạo phản hồi SourceCodeResponse
//        DocumentResponse response = new DocumentResponse();
//        response.setName(reward.getName());
//        response.setImage(reward.getImage());
//        response.setPrice(reward.getPrice());
//        response.setType(reward.getType());
//        response.setStatus(reward.getStatus());
//        response.setSectionList(sectionResponses);
//
//        // Trả về đối tượng SourceCodeResponse
//        return response;
//    }

//    @Transactional
//    public DocumentResponse createDocument(DocumentRequest request) {
//        if (request == null) {
//            throw new AppException(ErrorCode.REQUEST_NULL);
//        }
//
//        if (!request.getStatus().equals(DocumentStatus.ACTIVE.name()) &&
//                !request.getStatus().equals(DocumentStatus.INACTIVE.name())) {
//            throw new AppException(ErrorCode.WRONG_STATUS);
//        }
//
//        Reward reward = documentMapper.toDocument(request);
//        reward = unitOfWork.getDocumentRepository().save(reward);
//
//        List<SectionResponse> sectionResponses = new ArrayList<>();
//        int index = 0;
//
//        for (SectionRequest sectionRequest : request.getSectionList()) {
//            if (sectionRequest == null) {
//                continue;
//            }
//
//            Section section = sectionMapper.toSection(sectionRequest);
//            section.setCreatedDate(new Date());
//            section.setReward(reward);
//            section.setSectionOrder(index++);
//
//            section = unitOfWork.getSectionRepository().save(section);
//
//            List<ImageSectionResponse> imageResponses = new ArrayList<>();
//            for (ImageSectionRequest imageRequest : sectionRequest.getImageSectionList()) {
//                if (imageRequest == null || imageRequest.getUrl() == null) {
//                    continue;
//                }
//
//                ContentSection contentSection = new ContentSection();
//                contentSection.setUrl(imageRequest.getUrl());
//                contentSection.setSection(section);
//
//                contentSection = unitOfWork.getImageSectionRepository().save(contentSection);
//                imageResponses.add(new ImageSectionResponse(contentSection.getUrl()));
//            }
//
//            List<VideoSectionResponse> videoResponses = new ArrayList<>();
//            for (VideoSectionRequest videoRequest : sectionRequest.getVideoSectionList()) {
//                if (videoRequest == null || videoRequest.getUrl() == null) {
//                    continue;
//                }
//
//                VideoSection videoSection = new VideoSection();
//                videoSection.setUrl(videoRequest.getUrl());
//                videoSection.setSection(section);
//
//                videoSection = unitOfWork.getVideoSectionRepository().save(videoSection);
//                videoResponses.add(new VideoSectionResponse(videoSection.getUrl()));
//            }
//
//            SectionResponse sectionResponse = new SectionResponse();
//            sectionResponse.setLinkGit(section.getLinkGit());
//            sectionResponse.setImageSectionList(imageResponses);
//            sectionResponse.setVideoSectionList(videoResponses);
//
//            sectionResponses.add(sectionResponse);
//        }
//
//        DocumentResponse response = documentMapper.toResponse(reward);
//        response.setSectionList(sectionResponses);
//
//        return response;
//    }
//
//
//    @Transactional
//    public DocumentResponse update(UUID documentID, DocumentRequest request) {
//        if (!request.getStatus().equals(DocumentStatus.ACTIVE.name()) &&
//                !request.getStatus().equals(DocumentStatus.INACTIVE.name())) {
//            throw new AppException(ErrorCode.WRONG_STATUS);
//        }
//
//        Reward reward = unitOfWork.getDocumentRepository()
//                .findById(documentID)
//                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));
//
//        documentMapper.updateDocumentFromRequest(reward, request);
//
//        unitOfWork.getDocumentRepository().save(reward);
//
//        unitOfWork.getSectionRepository().deleteAllByDocument(reward);
//        reward.getSectionList().clear();
//
//        List<SectionResponse> sectionResponses = new ArrayList<>();
//        int index = 0;
//        for (SectionRequest sectionRequest : request.getSectionList()) {
//            Section section = sectionMapper.toSection(sectionRequest);
//            section.setReward(reward);
//            section.setSectionOrder(index++);
//
//            section = unitOfWork.getSectionRepository().save(section);
//
//            List<ImageSectionResponse> imageResponses = new ArrayList<>();
//            for (ImageSectionRequest imageRequest : sectionRequest.getImageSectionList()) {
//                ContentSection contentSection = new ContentSection();
//                contentSection.setUrl(imageRequest.getUrl());
//                contentSection.setSection(section);
//
//                contentSection = unitOfWork.getImageSectionRepository().save(contentSection);
//                imageResponses.add(new ImageSectionResponse(contentSection.getUrl()));
//            }
//
//            List<VideoSectionResponse> videoResponses = new ArrayList<>();
//            for (VideoSectionRequest videoRequest : sectionRequest.getVideoSectionList()) {
//                VideoSection videoSection = new VideoSection();
//                videoSection.setUrl(videoRequest.getUrl());
//                videoSection.setSection(section);
//
//                videoSection = unitOfWork.getVideoSectionRepository().save(videoSection);
//                videoResponses.add(new VideoSectionResponse(videoSection.getUrl()));
//            }
//
//            SectionResponse sectionResponse = new SectionResponse();
//            sectionResponse.setLinkGit(section.getLinkGit());
//            sectionResponse.setImageSectionList(imageResponses);
//            sectionResponse.setVideoSectionList(videoResponses);
//
//            sectionResponses.add(sectionResponse);
//        }
//
//        DocumentResponse response = documentMapper.toResponse(reward);
//        response.setSectionList(sectionResponses);
//
//        return response;
//    }

//    public List<DocumentResponse> getAll() {
//        return unitOfWork.getDocumentRepository().findAll().stream()
//                .map(document -> {
//                    DocumentResponse response = documentMapper.toResponse(document);
//                    // Sắp xếp Section theo order trước khi tạo response
//                    List<SectionResponse> sectionResponses = document.getSectionList().stream()
//                            .sorted(Comparator.comparingInt(Section::getSectionOrder))
//                            .map(documentMapper::toSectionResponse)
//                            .toList();
//                    response.setSectionList(sectionResponses);
//
//                    return response;
//                })
//                .toList();
//    }




    @Transactional
    public void deleteDocument(UUID documentId) {
        Reward reward = unitOfWork.getRewardRepository()
                .findById(documentId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));
        reward.getRedeemList().forEach(redeem -> {
            if(redeem.getReward().getDocumentId().equals(documentId))
                throw new AppException(ErrorCode.DOCUMENT_HAS_BEEN_USED);
        });
        unitOfWork.getRewardRepository().delete(reward);
    }
}

