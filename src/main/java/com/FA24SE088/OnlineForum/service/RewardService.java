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
import com.FA24SE088.OnlineForum.enums.RewardStatus;
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
                    mediaResponses.add(new MediaResponse(media.getNumber(),media.getLink()));
                }

                contentSectionResponses.add(new ContentSectionResponse(contentSection.getContent(), contentSection.getCode(),contentSection.getNumber(), mediaResponses));
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


    @Transactional
    public void deleteReward(UUID rewardId) {
        Reward reward = unitOfWork.getRewardRepository()
                .findById(rewardId)
                .orElseThrow(() -> new AppException(ErrorCode.REWARD_NOT_FOUND));
        reward.getRedeemList().forEach(redeem -> {
            if (redeem.getReward().getRewardId().equals(rewardId))
                throw new AppException(ErrorCode.DOCUMENT_HAS_BEEN_USED);
        });
        unitOfWork.getRewardRepository().delete(reward);
    }

    @Transactional
    public RewardResponse createReward(RewardRequest rewardRequest) {
        Reward reward = rewardMapper.toReward(rewardRequest);
        reward.setStatus(RewardStatus.ACTIVE.name());
//        reward.setName(rewardRequest.getName());
//        reward.setImage(rewardRequest.getImage());
//        reward.setPrice(rewardRequest.getPrice());
//        reward.setType(rewardRequest.getType());
//        reward.setStatus(rewardRequest.getStatus());

        List<Section> sections = new ArrayList<>();
        int sectionNumber = 1;

        for (SectionRequest sectionRequest : rewardRequest.getSectionList()) {
            Section section = new Section();
            section.setTitle(sectionRequest.getTitle());
            section.setNumber(sectionNumber++);
            section.setReward(reward);

            List<ContentSection> contentSections = new ArrayList<>();
            int contentSectionNumber = 1;

            for (ContentSectionRequest contentRequest : sectionRequest.getContentSectionList()) {
                ContentSection contentSection = new ContentSection();
                contentSection.setContent(contentRequest.getContent());
                contentSection.setCode(contentRequest.getCode());
                contentSection.setNumber(contentSectionNumber++);
                contentSection.setSection(section);

                List<Media> mediaList = new ArrayList<>();
                int mediaNumber = 1;

                for (MediaRequest mediaRequest : contentRequest.getMediaList()) {
                    Media media = new Media();
                    media.setLink(mediaRequest.getLink());
                    media.setNumber(mediaNumber++);
                    media.setContentSection(contentSection);
                    mediaList.add(media);
                }

                contentSection.setMedias(mediaList);
                contentSections.add(contentSection);
            }

            section.setContentSectionList(contentSections);
            sections.add(section);
        }

        reward.setSectionList(sections);
        Reward savedReward = unitOfWork.getRewardRepository().save(reward);

        return mapToRewardResponse(savedReward);
    }



    private RewardResponse mapToRewardResponse(Reward reward) {
        RewardResponse rewardResponse = new RewardResponse();
        rewardResponse.setRewardId(reward.getRewardId());
        rewardResponse.setName(reward.getName());
        rewardResponse.setImage(reward.getImage());
        rewardResponse.setPrice(reward.getPrice());
        rewardResponse.setType(reward.getType());
        rewardResponse.setStatus(reward.getStatus());

        List<SectionResponse> sectionResponses = reward.getSectionList().stream()
                .sorted(Comparator.comparingInt(Section::getNumber))
                .map(section -> {
                    SectionResponse sectionResponse = new SectionResponse();
                    sectionResponse.setTitle(section.getTitle());
                    sectionResponse.setNumber(section.getNumber());

                    List<ContentSectionResponse> contentResponses = section.getContentSectionList().stream()
                            .sorted(Comparator.comparingInt(ContentSection::getNumber))
                            .map(contentSection -> {
                                ContentSectionResponse contentSectionResponse = new ContentSectionResponse();
                                contentSectionResponse.setContent(contentSection.getContent());
                                contentSectionResponse.setCode(contentSection.getCode());
                                contentSectionResponse.setNumber(contentSection.getNumber());

                                List<MediaResponse> mediaResponses = contentSection.getMedias().stream()
                                        .sorted(Comparator.comparingInt(Media::getNumber))
                                        .map(media -> {
                                            MediaResponse mediaResponse = new MediaResponse();
                                            mediaResponse.setNumber(media.getNumber());
                                            mediaResponse.setLink(media.getLink());
                                            return mediaResponse;
                                        }).toList();

                                contentSectionResponse.setMediaList(mediaResponses);
                                return contentSectionResponse;
                            }).toList();

                    sectionResponse.setContentSectionResponses(contentResponses);
                    return sectionResponse;
                }).toList();

        rewardResponse.setSectionList(sectionResponses);
        return rewardResponse;
    }

    public List<RewardResponse> getAll() {
        return unitOfWork.getRewardRepository().findAll().stream()
                .map(this::mapToRewardResponse)
                .toList();
    }



}

