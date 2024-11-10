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
import org.springframework.security.core.context.SecurityContextHolder;
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
    public RewardResponse update(UUID rewardId, RewardRequest rewardRequest) {
        Reward reward = unitOfWork.getRewardRepository().findById(rewardId)
                .orElseThrow(() -> new AppException(ErrorCode.REWARD_NOT_FOUND));
        // Cập nhật thông tin cho Reward từ RewardRequest
        reward.setName(rewardRequest.getName());
        reward.setImage(rewardRequest.getImage());
        reward.setPrice(rewardRequest.getPrice());
        reward.setType(rewardRequest.getType());
        reward.setStatus(rewardRequest.getStatus());
        unitOfWork.getRewardRepository().save(reward);

        unitOfWork.getSectionRepository().deleteAllByReward(reward);
        reward.getSectionList().clear();

        // Tạo và thêm các Section mới từ request
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
        Reward updatedReward = unitOfWork.getRewardRepository().saveAndFlush(reward);

        return mapToRewardResponse(updatedReward);
    }

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
    private Account getCurrentUser() {
        var context = SecurityContextHolder.getContext();
        return unitOfWork.getAccountRepository().findByUsername(context.getAuthentication().getName()).orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }
    public List<RewardResponse> getUnredeemedRewardsForCurrentUser() {
        Account currentUser = getCurrentUser();

        List<UUID> redeemedRewardIds = unitOfWork.getRedeemRepository()
                .findAllByAccount(currentUser).stream()
                .map(redeem -> redeem.getReward().getRewardId())
                .toList();
        return unitOfWork.getRewardRepository().findAll().stream()
                .filter(reward -> !redeemedRewardIds.contains(reward.getRewardId()))
                .map(this::mapToRewardResponse)
                .toList();
    }

}

