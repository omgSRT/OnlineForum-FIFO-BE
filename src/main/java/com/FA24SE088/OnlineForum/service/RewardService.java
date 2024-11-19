package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.ByteArrayResource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

import com.FA24SE088.OnlineForum.dto.response.RewardResponse;;
import com.FA24SE088.OnlineForum.entity.*;
import com.FA24SE088.OnlineForum.enums.RewardStatus;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.RewardMapper;
import com.FA24SE088.OnlineForum.mapper.SectionMapper;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import lombok.AccessLevel;
import org.springframework.security.core.context.SecurityContextHolder;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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

//    @Transactional
//    public RewardResponse update(UUID rewardId, RewardRequest rewardRequest) {
//        Reward reward = unitOfWork.getRewardRepository().findById(rewardId)
//                .orElseThrow(() -> new AppException(ErrorCode.REWARD_NOT_FOUND));
//        // Cập nhật thông tin cho Reward từ RewardRequest
//        reward.setName(rewardRequest.getName());
//        reward.setImage(rewardRequest.getImage());
//        reward.setPrice(rewardRequest.getPrice());
//        reward.setType(rewardRequest.getType());
//        reward.setStatus(rewardRequest.getStatus());
//        unitOfWork.getRewardRepository().save(reward);
//
//        unitOfWork.getSectionRepository().deleteAllByReward(reward);
//        reward.getSectionList().clear();
//
//        // Tạo và thêm các Section mới từ request
//        List<Section> sections = new ArrayList<>();
//        int sectionNumber = 1;
//
//        for (SectionRequest sectionRequest : rewardRequest.getSectionList()) {
//            Section section = new Section();
//            section.setTitle(sectionRequest.getTitle());
//            section.setNumber(sectionNumber++);
//            section.setReward(reward);
//
//            List<ContentSection> contentSections = new ArrayList<>();
//            int contentSectionNumber = 1;
//
//            for (ContentSectionRequest contentRequest : sectionRequest.getContentSectionList()) {
//                ContentSection contentSection = new ContentSection();
//                contentSection.setContent(contentRequest.getContent());
//                contentSection.setCode(contentRequest.getCode());
//                contentSection.setNumber(contentSectionNumber++);
//                contentSection.setSection(section);
//
//                List<Media> mediaList = new ArrayList<>();
//                int mediaNumber = 1;
//
//                for (MediaRequest mediaRequest : contentRequest.getMediaList()) {
//                    Media media = new Media();
//                    media.setLink(mediaRequest.getLink());
//                    media.setNumber(mediaNumber++);
//                    media.setContentSection(contentSection);
//                    mediaList.add(media);
//                }
//
//                contentSection.setMedias(mediaList);
//                contentSections.add(contentSection);
//            }
//
//            section.setContentSectionList(contentSections);
//            sections.add(section);
//        }
//        reward.setSectionList(sections);
//        Reward updatedReward = unitOfWork.getRewardRepository().saveAndFlush(reward);
//
//        return mapToRewardResponse(updatedReward);
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

//    @Transactional
//    public RewardResponse createReward(RewardRequest rewardRequest) {
//        Reward reward = rewardMapper.toReward(rewardRequest);
//        reward.setStatus(RewardStatus.ACTIVE.name());
//
//        List<Section> sections = new ArrayList<>();
//        int sectionNumber = 1;
//
//        for (SectionRequest sectionRequest : rewardRequest.getSectionList()) {
//            Section section = new Section();
//            section.setTitle(sectionRequest.getTitle());
//            section.setNumber(sectionNumber++);
//            section.setReward(reward);
//
//            List<ContentSection> contentSections = new ArrayList<>();
//            int contentSectionNumber = 1;
//
//            for (ContentSectionRequest contentRequest : sectionRequest.getContentSectionList()) {
//                ContentSection contentSection = new ContentSection();
//                contentSection.setContent(contentRequest.getContent());
//                contentSection.setCode(contentRequest.getCode());
//                contentSection.setNumber(contentSectionNumber++);
//                contentSection.setSection(section);
//
//                List<Media> mediaList = new ArrayList<>();
//                int mediaNumber = 1;
//
//                for (MediaRequest mediaRequest : contentRequest.getMediaList()) {
//                    Media media = new Media();
//                    media.setLink(mediaRequest.getLink());
//                    media.setNumber(mediaNumber++);
//                    media.setContentSection(contentSection);
//                    mediaList.add(media);
//                }
//
//                contentSection.setMedias(mediaList);
//                contentSections.add(contentSection);
//            }
//
//            section.setContentSectionList(contentSections);
//            sections.add(section);
//        }
//
//        reward.setSectionList(sections);
//        Reward savedReward = unitOfWork.getRewardRepository().save(reward);
//
//        return mapToRewardResponse(savedReward);
//    }


//    private RewardResponse mapToRewardResponse(Reward reward) {
//        RewardResponse rewardResponse = new RewardResponse();
//        rewardResponse.setRewardId(reward.getRewardId());
//        rewardResponse.setName(reward.getName());
//        rewardResponse.setImage(reward.getImage());
//        rewardResponse.setPrice(reward.getPrice());
//        rewardResponse.setType(reward.getType());
//        rewardResponse.setStatus(reward.getStatus());
//
//        List<SectionResponse> sectionResponses = reward.getSectionList().stream()
//                .sorted(Comparator.comparingInt(Section::getNumber))
//                .map(section -> {
//                    SectionResponse sectionResponse = new SectionResponse();
//                    sectionResponse.setTitle(section.getTitle());
//                    sectionResponse.setNumber(section.getNumber());
//
//                    List<ContentSectionResponse> contentResponses = section.getContentSectionList().stream()
//                            .sorted(Comparator.comparingInt(ContentSection::getNumber))
//                            .map(contentSection -> {
//                                ContentSectionResponse contentSectionResponse = new ContentSectionResponse();
//                                contentSectionResponse.setContent(contentSection.getContent());
//                                contentSectionResponse.setCode(contentSection.getCode());
//                                contentSectionResponse.setNumber(contentSection.getNumber());
//
//                                List<MediaResponse> mediaResponses = contentSection.getMedias().stream()
//                                        .sorted(Comparator.comparingInt(Media::getNumber))
//                                        .map(media -> {
//                                            MediaResponse mediaResponse = new MediaResponse();
//                                            mediaResponse.setNumber(media.getNumber());
//                                            mediaResponse.setLink(media.getLink());
//                                            return mediaResponse;
//                                        }).toList();
//
//                                contentSectionResponse.setMediaList(mediaResponses);
//                                return contentSectionResponse;
//                            }).toList();
//
//                    sectionResponse.setContentSectionResponses(contentResponses);
//                    return sectionResponse;
//                }).toList();
//
//        rewardResponse.setSectionList(sectionResponses);
//        return rewardResponse;
//    }

    //    public List<RewardResponse> getAll() {
//        return unitOfWork.getRewardRepository().findAll().stream()
//                .map(this::mapToRewardResponse)
//                .toList();
//    }
//
    public RewardResponse getById(UUID id) {
        return unitOfWork.getRewardRepository().findById(id).map(rewardMapper::toResponse).orElseThrow(() -> new AppException(ErrorCode.REWARD_NOT_FOUND));
    }

    public RewardResponse create(RewardRequest rewardRequest) {
        if (rewardRequest.getName() == null) {
            throw new AppException(ErrorCode.REWARD_INVALID_NAME);
        }
        if (rewardRequest.getPrice() < 10) {
            throw new AppException(ErrorCode.REWARD_INVALID_PRICE);
        }

        Reward reward = rewardMapper.toReward(rewardRequest);
        reward.setStatus(RewardStatus.ACTIVE.name());
        reward.setCreatedDate(new Date());

        unitOfWork.getRewardRepository().save(reward);

        return rewardMapper.toResponse(reward);
    }

    public RewardResponse update(UUID rewardId, RewardUpdateRequest rewardRequest) {
        Reward reward = unitOfWork.getRewardRepository().findById(rewardId).orElseThrow(() -> new AppException(ErrorCode.REWARD_NOT_FOUND));
        if (rewardRequest.getName() != null && !rewardRequest.getName().isEmpty()) {
            reward.setName(reward.getName());
        }
        if (rewardRequest.getImage() != null && !rewardRequest.getImage().isEmpty()) {
            reward.setImage(rewardRequest.getImage());
        }
        reward.setPrice(rewardRequest.getPrice());
        if (rewardRequest.getDescription() != null && !rewardRequest.getDescription().isEmpty()) {
            reward.setDescription(rewardRequest.getDescription());
        }
        if (rewardRequest.getStatus() != null) {
            reward.setStatus(rewardRequest.getStatus().name());
        }
        if (rewardRequest.getLinkSourceCode() != null && !rewardRequest.getLinkSourceCode().isEmpty()) {
            reward.setLinkSourceCode(rewardRequest.getLinkSourceCode());
        }
        unitOfWork.getRewardRepository().save(reward);
        return rewardMapper.toResponse(reward);
    }


    private Account getCurrentUser() {
        var context = SecurityContextHolder.getContext();
        return unitOfWork.getAccountRepository().findByUsername(context.getAuthentication().getName()).orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    public List<RewardResponse> getAllRewardOfCurrentUser() {
        Account currentUser = getCurrentUser();
        return unitOfWork.getRedeemRepository().findAllByAccount(currentUser)
                .stream()
                .map(Redeem::getReward)
                .sorted(Comparator.comparing(Reward::getCreatedDate).reversed()) // Sắp xếp theo ngày mới nhất giảm dần
                .map(rewardMapper::toResponse)
                .toList();

    }

    public List<RewardResponse> getAll() {
        return unitOfWork.getRewardRepository().findAll().stream().map(rewardMapper::toResponse)
                .toList();
    }

    public List<RewardResponse> getUnredeemedRewardsForCurrentUser() {
        Account currentUser = getCurrentUser();
        List<UUID> redeemedRewardIds = unitOfWork.getRedeemRepository()
                .findAllByAccount(currentUser).stream()
                .map(redeem -> redeem.getReward().getRewardId())
                .toList();
        return unitOfWork.getRewardRepository().findAll().stream()
                .filter(reward -> !redeemedRewardIds.contains(reward.getRewardId()))
                .sorted(Comparator.comparing(Reward::getCreatedDate).reversed())
                .map(rewardMapper::toResponse)
                .toList();
    }

    public ResponseEntity<Resource> downloadFileSourceCode(UUID rewardId) {
        // Lấy Reward từ cơ sở dữ liệu
        Reward reward = unitOfWork.getRewardRepository().findById(rewardId)
                .orElseThrow(() -> new AppException(ErrorCode.REWARD_NOT_FOUND));

        if (!RewardStatus.ACTIVE.name().equalsIgnoreCase(reward.getStatus())) {
            throw new AppException(ErrorCode.REWARD_NOT_AVAILABLE);
        }

        String linkSourceCode = reward.getLinkSourceCode();

        try {
            Resource fileResource;

            // Kiểm tra xem link là URL hay đường dẫn cục bộ
            if (linkSourceCode.startsWith("http://") || linkSourceCode.startsWith("https://")) {
                // Xử lý URL (tải tệp từ URL)
                URL url = new URL(linkSourceCode);
                fileResource = new UrlResource(url);
            } else {
                // Xử lý đường dẫn cục bộ
                Path filePath = Paths.get(linkSourceCode);
                if (!Files.exists(filePath)) {
                    throw new AppException(ErrorCode.FILE_NOT_FOUND);
                }
                fileResource = new UrlResource(filePath.toUri());
            }

            if (!fileResource.exists() || !fileResource.isReadable()) {
                throw new AppException(ErrorCode.FILE_NOT_READABLE);
            }

            // Tạo tệp ZIP tạm thời (dùng phương thức trong mã của bạn để nén tệp vào ZIP nếu cần)
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            AtomicBoolean isZipEmpty = new AtomicBoolean(true);

            try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
                String fileName = linkSourceCode.substring(linkSourceCode.lastIndexOf('/') + 1); // Tên tệp gốc

                // Thêm file vào file zip
                ZipEntry zipEntry = new ZipEntry(fileName);
                zipOutputStream.putNextEntry(zipEntry);

                // Đọc dữ liệu tệp và ghi vào ZipOutputStream
                try (InputStream inputStream = fileResource.getInputStream()) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) >= 0) {
                        zipOutputStream.write(buffer, 0, length);
                    }
                }

                zipOutputStream.closeEntry(); // Đóng entry trong ZIP
                isZipEmpty.set(false);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi tạo file zip.", e);
            }

            if (isZipEmpty.get()) {
                throw new AppException(ErrorCode.NO_FILES_TO_DOWNLOAD);
            }

            // Trả về file ZIP dưới dạng ResponseEntity
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + "reward-source-code.zip" + "\"")
                    .body(new ByteArrayResource(byteArrayOutputStream.toByteArray()));

        } catch (MalformedURLException e) {
            throw new RuntimeException("Lỗi khi truy cập URL file.", e);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi xử lý tệp nguồn.", e);
        }
    }



}

