package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.RewardRequest;
import com.FA24SE088.OnlineForum.dto.response.*;
import com.FA24SE088.OnlineForum.entity.Reward;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RewardMapper {

    @Mapping(target = "linkSourceCode", source = "linkSourceCode")
    Reward toReward(RewardRequest request);

    @Mapping(target = "rewardId", source = "rewardId")
    @Mapping(target = "createdDate", source = "createdDate")
    RewardResponse toResponse(Reward reward);
    void updateRewardFromRequest (@MappingTarget Reward reward, RewardRequest request);



}
