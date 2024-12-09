package com.FA24SE088.OnlineForum.mapper;


import com.FA24SE088.OnlineForum.dto.request.PricingRequest;
import com.FA24SE088.OnlineForum.dto.response.PricingResponse;
import com.FA24SE088.OnlineForum.entity.MonkeyCoinPack;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MonkeyCoinPackMapper {

    MonkeyCoinPack toPricing(PricingRequest request);

    @Mapping(target = "imgUrl", source = "imgUrl")
    PricingResponse toResponse(MonkeyCoinPack monkeyCoinPack);
}
