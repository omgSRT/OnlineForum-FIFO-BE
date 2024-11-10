package com.FA24SE088.OnlineForum.mapper;


import com.FA24SE088.OnlineForum.dto.request.PricingRequest;
import com.FA24SE088.OnlineForum.dto.response.PricingResponse;
import com.FA24SE088.OnlineForum.entity.MonkeyCoinPack;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MonkeyCoinPackMapper {

    MonkeyCoinPack toPricing(PricingRequest request);
    PricingResponse toResponse(MonkeyCoinPack monkeyCoinPack);
}
