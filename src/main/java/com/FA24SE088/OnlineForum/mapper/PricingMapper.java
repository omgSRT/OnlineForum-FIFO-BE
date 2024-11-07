package com.FA24SE088.OnlineForum.mapper;


import com.FA24SE088.OnlineForum.dto.request.PricingRequest;
import com.FA24SE088.OnlineForum.dto.response.PricingResponse;
import com.FA24SE088.OnlineForum.entity.Pricing;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PricingMapper {

    Pricing toPricing(PricingRequest request);
    PricingResponse toResponse(Pricing pricing);
}
