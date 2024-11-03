package com.FA24SE088.OnlineForum.mapper;

import com.FA24SE088.OnlineForum.dto.request.TypeBonusRequest;
import com.FA24SE088.OnlineForum.dto.request.TypeBonusUpdateRequest;
import com.FA24SE088.OnlineForum.dto.response.TypeBonusResponse;
import com.FA24SE088.OnlineForum.entity.TypeBonus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TypeBonusMapper {
    TypeBonus toTypeBonus(TypeBonusRequest request);

    TypeBonusResponse toTypeBonusResponse(TypeBonus typeBonus);

    @Mapping(target = "dailyPointList", ignore = true)
    void updateTypeBonus(@MappingTarget TypeBonus typeBonus, TypeBonusUpdateRequest request);
}
