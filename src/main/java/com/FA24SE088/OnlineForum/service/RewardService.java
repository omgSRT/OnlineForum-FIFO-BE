package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.ContentSectionRequest;
import com.FA24SE088.OnlineForum.dto.request.MediaRequest;
import com.FA24SE088.OnlineForum.dto.request.RewardRequest;
import com.FA24SE088.OnlineForum.dto.request.SectionRequest;
import com.FA24SE088.OnlineForum.dto.response.ContentSectionResponse;
import com.FA24SE088.OnlineForum.dto.response.MediaResponse;
import com.FA24SE088.OnlineForum.dto.response.RewardResponse;
import com.FA24SE088.OnlineForum.dto.response.SectionResponse;
import com.FA24SE088.OnlineForum.entity.*;
import com.FA24SE088.OnlineForum.enums.DocumentStatus;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.RewardMapper;
import com.FA24SE088.OnlineForum.mapper.SectionMapper;
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
public class RewardService {
    UnitOfWork unitOfWork;
    RewardMapper rewardMapper;
    SectionMapper sectionMapper;

    @Transactional
    public RewardResponse create(RewardRequest request) {
        if (request == null) {
            throw new AppException(ErrorCode.REQUEST_NULL);
        }

        if (!request.getStatus().equals(DocumentStatus.ACTIVE.name()) &&
                !request.getStatus().equals(DocumentStatus.INACTIVE.name())) {
            throw new AppException(ErrorCode.WRONG_STATUS);
        }

        Reward reward = rewardMapper.toReward(request);
        reward = unitOfWork.getRewardRepository().save(reward);

        List<SectionResponse> sectionResponses = new ArrayList<>();
        for (int sectionIndex = 0; sectionIndex < request.getSectionList().size(); sectionIndex++) {
            SectionRequest sectionRequest = request.getSectionList().get(sectionIndex);
            if (sectionRequest == null) continue;

            Section section = sectionMapper.toSection(sectionRequest);
            section.setReward(reward);
            section.setNumber(sectionIndex); // Thiết lập field number cho Section
            section = unitOfWork.getSectionRepository().save(section);

            List<ContentSectionResponse> contentSectionResponses = new ArrayList<>();

            for (int contentIndex = 0; contentIndex < sectionRequest.getContentSectionList().size(); contentIndex++) {
                ContentSectionRequest contentSectionRequest = sectionRequest.getContentSectionList().get(contentIndex);
                if (contentSectionRequest == null) continue;

                // Tạo ContentSection và thiết lập field number
                ContentSection contentSection = ContentSection.builder()
                        .content(contentSectionRequest.getContent())
                        .code(contentSectionRequest.getCode())
                        .section(section)
                        .number(contentIndex) // Thiết lập field number cho ContentSection
                        .build();

                // Lưu ContentSection
                contentSection = unitOfWork.getContentSectionRepository().save(contentSection);

                List<MediaResponse> mediaResponses = new ArrayList<>();
                for (int mediaIndex = 0; mediaIndex < contentSectionRequest.getMediaList().size(); mediaIndex++) {
                    MediaRequest mediaRequest = contentSectionRequest.getMediaList().get(mediaIndex);
                    if (mediaRequest == null) continue;

                    // Tạo Media và thiết lập field number
                    Media media = Media.builder()
                            .link(mediaRequest.getLink())
                            .contentSection(contentSection)
                            .number(mediaIndex) // Thiết lập field number cho Media
                            .build();

                    // Lưu Media
                    media = unitOfWork.getMediaRepository().save(media);
                    mediaResponses.add(new MediaResponse(media.getLink()));
                }

                // Thêm ContentSectionResponse
                contentSectionResponses.add(new ContentSectionResponse(contentSection.getContent(), contentSection.getCode(), mediaResponses));
            }

            // Thêm SectionResponse
            SectionResponse sectionResponse = SectionResponse.builder()
                    .title(section.getTitle())
                    .contentSectionResponses(contentSectionResponses)
                    .build();
            sectionResponses.add(sectionResponse);
        }

        RewardResponse response = rewardMapper.toResponse(reward);
        response.setSectionList(sectionResponses);
        return response;
    }



    @Transactional(readOnly = true)
    public List<RewardResponse> getAll() {
        List<Reward> rewards = unitOfWork.getRewardRepository().findAll(); // Retrieve all rewards from the database
        List<RewardResponse> rewardResponses = new ArrayList<>();

        for (Reward reward : rewards) {
            List<SectionResponse> sectionResponses = new ArrayList<>();

            for (Section section : reward.getSectionList()) {
                List<ContentSectionResponse> contentSectionResponses = new ArrayList<>();

                for (ContentSection contentSection : section.getContentSectionList()) {
                    List<MediaResponse> mediaResponses = new ArrayList<>();

                    for (Media media : contentSection.getMedias()) { // Assuming `ContentSection` has a getMediaList() method
                        mediaResponses.add(new MediaResponse(media.getLink()));
                    }

                    contentSectionResponses.add(new ContentSectionResponse(
                            contentSection.getContent(),
                            contentSection.getCode(),
                            mediaResponses
                    ));
                }

                SectionResponse sectionResponse = SectionResponse.builder()
                        .title(section.getTitle())
                        .contentSectionResponses(contentSectionResponses)
                        .build();
                sectionResponses.add(sectionResponse);
            }

            RewardResponse rewardResponse = RewardResponse.builder()
                    .documentId(reward.getDocumentId())
                    .name(reward.getName())
                    .image(reward.getImage())
                    .price(reward.getPrice())
                    .type(reward.getType())
                    .status(reward.getStatus())
                    .sectionList(sectionResponses)
                    .build();

            rewardResponses.add(rewardResponse);
        }

        return rewardResponses;
    }

    @Transactional
    public RewardResponse update(UUID documentID, RewardRequest request) {
        if (request == null) {
            throw new AppException(ErrorCode.REQUEST_NULL);
        }

        if (!request.getStatus().equals(DocumentStatus.ACTIVE.name()) &&
                !request.getStatus().equals(DocumentStatus.INACTIVE.name())) {
            throw new AppException(ErrorCode.WRONG_STATUS);
        }

        Reward reward = unitOfWork.getRewardRepository()
                .findById(documentID)
                .orElseThrow(() -> new AppException(ErrorCode.REWARD_NOT_FOUND));

        rewardMapper.updateRewardFromRequest(reward, request);
        unitOfWork.getRewardRepository().save(reward);


        unitOfWork.getSectionRepository().deleteAllByReward(reward);
        reward.getSectionList().clear();

        List<SectionResponse> sectionResponses = new ArrayList<>();
        int index = 0;

        for (SectionRequest sectionRequest : request.getSectionList()) {
            if (sectionRequest == null) continue;

            Section section = sectionMapper.toSection(sectionRequest);
            section.setReward(reward);
            section.setNumber(index++);
            section = unitOfWork.getSectionRepository().save(section);

            List<ContentSectionResponse> contentSectionResponses = new ArrayList<>();
            int contentIndex = 0;

            for (ContentSectionRequest contentSectionRequest : sectionRequest.getContentSectionList()) {
                if (contentSectionRequest == null) continue;

                ContentSection contentSection = ContentSection.builder()
                        .content(contentSectionRequest.getContent())
                        .code(contentSectionRequest.getCode())
                        .section(section)
                        .number(contentIndex++)
                        .build();
                contentSection = unitOfWork.getContentSectionRepository().save(contentSection);

                List<MediaResponse> mediaResponses = new ArrayList<>();
                int mediaIndex = 0;

                for (MediaRequest mediaRequest : contentSectionRequest.getMediaList()) {
                    if (mediaRequest == null) continue;

                    Media media = Media.builder()
                            .link(mediaRequest.getLink())
                            .contentSection(contentSection)
                            .number(mediaIndex++)
                            .build();
                    media = unitOfWork.getMediaRepository().save(media);
                    mediaResponses.add(new MediaResponse(media.getLink()));
                }

                contentSectionResponses.add(new ContentSectionResponse(contentSection.getContent(), contentSection.getCode(), mediaResponses));
            }

            SectionResponse sectionResponse = SectionResponse.builder()
                    .title(section.getTitle())
                    .contentSectionResponses(contentSectionResponses)
                    .build();
            sectionResponses.add(sectionResponse);
        }

        RewardResponse response = rewardMapper.toResponse(reward);
        response.setSectionList(sectionResponses);
        return response;
    }


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
                .orElseThrow(() -> new AppException(ErrorCode.REWARD_NOT_FOUND));
        reward.getRedeemList().forEach(redeem -> {
            if (redeem.getReward().getDocumentId().equals(documentId))
                throw new AppException(ErrorCode.DOCUMENT_HAS_BEEN_USED);
        });
        unitOfWork.getRewardRepository().delete(reward);
    }
}

