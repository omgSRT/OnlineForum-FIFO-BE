package com.FA24SE088.OnlineForum.service;


import com.FA24SE088.OnlineForum.dto.request.PricingRequest;
import com.FA24SE088.OnlineForum.dto.response.PricingResponse;
import com.FA24SE088.OnlineForum.entity.MonkeyCoinPack;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.MonkeyCoinPackMapper;
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
public class MonkeyCoinPackService {

    UnitOfWork unitOfWork;
    MonkeyCoinPackMapper monkeyCoinPackMapper; // Bạn cần tạo mapper để chuyển đổi entity sang response nếu muốn.

    public PricingResponse createPricing(PricingRequest pricingRequest) {
        MonkeyCoinPack monkeyCoinPack = monkeyCoinPackMapper.toPricing(pricingRequest);
        monkeyCoinPack.setImgUrl(pricingRequest.getImgUrl());
        MonkeyCoinPack savedMonkeyCoinPack = unitOfWork.getMonkeyCoinPackRepository().save(monkeyCoinPack);
        return monkeyCoinPackMapper.toResponse(savedMonkeyCoinPack);
    }

    public PricingResponse updatePricing(UUID pricingId, PricingRequest pricingRequest) {
        MonkeyCoinPack monkeyCoinPack = unitOfWork.getMonkeyCoinPackRepository().findById(pricingId).orElseThrow(() -> new AppException(ErrorCode.PRICING_NOT_FOUND));
        monkeyCoinPack.setImgUrl(pricingRequest.getImgUrl());
        monkeyCoinPack.setPrice(pricingRequest.getPrice());
        monkeyCoinPack.setPoint(monkeyCoinPack.getPoint());
        MonkeyCoinPack savedMonkeyCoinPack = unitOfWork.getMonkeyCoinPackRepository().save(monkeyCoinPack);
        return monkeyCoinPackMapper.toResponse(savedMonkeyCoinPack);
    }

    public Optional<PricingResponse> getPricingById(UUID pricingId) {
        return unitOfWork.getMonkeyCoinPackRepository()
                .findById(pricingId)
                .map(monkeyCoinPackMapper::toResponse);
    }

    public List<PricingResponse> getAllPricings() {
        List<MonkeyCoinPack> monkeyCoinPacks = unitOfWork.getMonkeyCoinPackRepository().findAll();
        return monkeyCoinPacks.stream().map(monkeyCoinPackMapper::toResponse).toList();
    }

    public void deletePricing(UUID pricingId) {
        if (unitOfWork.getMonkeyCoinPackRepository().existsById(pricingId)) {
            unitOfWork.getMonkeyCoinPackRepository().deleteById(pricingId);
        } else {
            throw new AppException(ErrorCode.PRICING_NOT_FOUND);
        }
    }
}
