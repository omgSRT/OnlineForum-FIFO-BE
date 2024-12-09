package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

import com.FA24SE088.OnlineForum.dto.response.RewardResponse;;
import com.FA24SE088.OnlineForum.entity.*;
import com.FA24SE088.OnlineForum.enums.RewardStatus;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.RewardMapper;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import lombok.AccessLevel;
import org.springframework.security.core.context.SecurityContextHolder;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;


@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class RewardService {
    UnitOfWork unitOfWork;
    RewardMapper rewardMapper;

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

    public byte[] downloadFileSourceCode(UUID rewardId) {
        // Lấy thông tin người dùng hiện tại
        Account currentUser = getCurrentUser();

        // Tìm Reward theo rewardId
        Reward reward = unitOfWork.getRewardRepository().findById(rewardId)
                .orElseThrow(() -> new AppException(ErrorCode.REWARD_NOT_FOUND));

        if (!RewardStatus.ACTIVE.name().equalsIgnoreCase(reward.getStatus())) {
            throw new AppException(ErrorCode.REWARD_NOT_AVAILABLE);
        }

        String linkSourceCode = reward.getLinkSourceCode();

        //tách đường dẫn lấy file ra từ link
        String filePath = linkSourceCode.split("image-description-detail.appspot.com/o/")[1].split("\\?")[0];

        //giải mã đường dẫn nếu vẫn còn %2F, %20, etc.
        String decodedFilePath = URLDecoder.decode(filePath, StandardCharsets.UTF_8);

        //demo link
        //https://firebasestorage.googleapis.com/v0/b/image-description-detail.appspot.com/o/Post%20image%2Ftest02-master.zip?alt=media&token=7b0c7c14-b1e4-4426-b2b9-0bf9b271c177
        //gọi firebase
        Bucket bucket = StorageClient.getInstance().bucket();


        Blob blob = bucket.get(decodedFilePath);

        if (blob == null) {
            return null;
        }

        return blob.getContent();
    }



}

