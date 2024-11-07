package com.FA24SE088.OnlineForum.service;


import com.FA24SE088.OnlineForum.dto.request.PricingRequest;
import com.FA24SE088.OnlineForum.dto.response.PricingResponse;
import com.FA24SE088.OnlineForum.entity.Pricing;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.PricingMapper;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class PricingService {

    UnitOfWork unitOfWork;
    PricingMapper pricingMapper; // Bạn cần tạo mapper để chuyển đổi entity sang response nếu muốn.

    public PricingResponse createPricing(PricingRequest pricingRequest) {
        Pricing pricing = pricingMapper.toPricing(pricingRequest);
        Pricing savedPricing = unitOfWork.getPricingRepository().save(pricing);
        return pricingMapper.toResponse(savedPricing);
    }

    public PricingResponse updatePricing(UUID pricingId, PricingRequest pricingRequest) {
        Pricing pricing = unitOfWork.getPricingRepository().findById(pricingId).orElseThrow(() -> new AppException(ErrorCode.PRICING_NOT_FOUND));
        pricing.setPrice(pricingRequest.getPrice());
        pricing.setPoint(pricing.getPoint());
        Pricing savedPricing = unitOfWork.getPricingRepository().save(pricing);
        return pricingMapper.toResponse(savedPricing);
    }

    public Optional<PricingResponse> getPricingById(UUID pricingId) {
        return unitOfWork.getPricingRepository()
                .findById(pricingId)
                .map(pricingMapper::toResponse);
    }

    public List<PricingResponse> getAllPricings() {
        List<Pricing> pricings = unitOfWork.getPricingRepository().findAll();
        return pricings.stream().map(pricingMapper::toResponse).toList();
    }

    public void deletePricing(UUID pricingId) {
        if (unitOfWork.getPricingRepository().existsById(pricingId)) {
            unitOfWork.getPricingRepository().deleteById(pricingId);
        } else {
            throw new AppException(ErrorCode.PRICING_NOT_FOUND);
        }
    }
}
