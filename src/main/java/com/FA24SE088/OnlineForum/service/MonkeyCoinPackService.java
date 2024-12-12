package com.FA24SE088.OnlineForum.service;


import com.FA24SE088.OnlineForum.dto.request.MonkeyCoinPackRequest;
import com.FA24SE088.OnlineForum.dto.response.MonkeyCoinPackResponse;
import com.FA24SE088.OnlineForum.entity.MonkeyCoinPack;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.MonkeyCoinPackMapper;
import com.FA24SE088.OnlineForum.repository.MonkeyCoinPackRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class MonkeyCoinPackService {
    MonkeyCoinPackRepository monkeyCoinPackRepository;
    MonkeyCoinPackMapper monkeyCoinPackMapper;

    public MonkeyCoinPackResponse createMonkeyCoinPack(MonkeyCoinPackRequest monkeyCoinPackRequest) {
        MonkeyCoinPack monkeyCoinPack = monkeyCoinPackMapper.toMonkeyCoinPack(monkeyCoinPackRequest);
        monkeyCoinPack.setImgUrl(monkeyCoinPackRequest.getImgUrl());
        MonkeyCoinPack savedMonkeyCoinPack = monkeyCoinPackRepository.save(monkeyCoinPack);
        return monkeyCoinPackMapper.toResponse(savedMonkeyCoinPack);
    }

    public MonkeyCoinPackResponse updateMonkeyCoinPack(UUID monkeyCoinPackId, MonkeyCoinPackRequest monkeyCoinPackRequest) {
        MonkeyCoinPack monkeyCoinPack = monkeyCoinPackRepository.findById(monkeyCoinPackId).orElseThrow(() -> new AppException(ErrorCode.MONKEY_COIN_PACK_NOT_FOUND));
        monkeyCoinPack.setImgUrl(monkeyCoinPackRequest.getImgUrl());
        monkeyCoinPack.setPrice(monkeyCoinPackRequest.getPrice());
        monkeyCoinPack.setPoint(monkeyCoinPack.getPoint());
        MonkeyCoinPack savedMonkeyCoinPack = monkeyCoinPackRepository.save(monkeyCoinPack);
        return monkeyCoinPackMapper.toResponse(savedMonkeyCoinPack);
    }

    public Optional<MonkeyCoinPackResponse> getMonkeyCoinPackById(UUID monkeyCoinPackId) {
        return monkeyCoinPackRepository
                .findById(monkeyCoinPackId)
                .map(monkeyCoinPackMapper::toResponse);
    }

    public List<MonkeyCoinPackResponse> getAllMonkeyCoinPack() {
        List<MonkeyCoinPack> monkeyCoinPacks = monkeyCoinPackRepository.findAll();
        return monkeyCoinPacks.stream()
                .sorted(Comparator.comparingLong(MonkeyCoinPack::getPrice).reversed())
                .map(monkeyCoinPackMapper::toResponse)
                .toList();
    }

    public void deleteMonkeyCoinPack(UUID monkeyCoinPackId) {
        if (monkeyCoinPackRepository.existsById(monkeyCoinPackId)) {
            monkeyCoinPackRepository.deleteById(monkeyCoinPackId);
        } else {
            throw new AppException(ErrorCode.MONKEY_COIN_PACK_NOT_FOUND);
        }
    }
}
