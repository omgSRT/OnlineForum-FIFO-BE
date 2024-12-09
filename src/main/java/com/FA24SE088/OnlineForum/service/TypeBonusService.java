package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.TypeBonusRequest;
import com.FA24SE088.OnlineForum.dto.request.TypeBonusUpdateRequest;
import com.FA24SE088.OnlineForum.dto.response.TypeBonusResponse;
import com.FA24SE088.OnlineForum.entity.TypeBonus;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.TypeBonusMapper;
import com.FA24SE088.OnlineForum.repository.TypeBonusRepository;
import com.FA24SE088.OnlineForum.utils.PaginationUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class TypeBonusService {
    TypeBonusRepository typeBonusRepository;
    TypeBonusMapper typeBonusMapper;
    PaginationUtils paginationUtils;

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public CompletableFuture<TypeBonusResponse> createTypeBonus(TypeBonusRequest request) {
        var foundTypeBonusFuture = findByNameAndQuantity(request.getName().name(), request.getQuantity());

        return foundTypeBonusFuture.thenApply(foundTypeBonus -> {
            if (foundTypeBonus != null) {
                throw new AppException(ErrorCode.TYPE_BONUS_ALREADY_EXIST);
            }

            TypeBonus newTypeBonus = typeBonusMapper.toTypeBonus(request);
            newTypeBonus.setDailyPointList(new ArrayList<>());

            return typeBonusMapper.toTypeBonusResponse(typeBonusRepository.save(newTypeBonus));
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public CompletableFuture<List<TypeBonusResponse>> getAllTypeBonuses(int page, int perPage, String name) {
        return CompletableFuture.supplyAsync(() -> {
            var list = typeBonusRepository.findAll().stream()
                    .filter(typeBonus -> name == null || typeBonus.getName().contains(name))
                    .map(typeBonusMapper::toTypeBonusResponse)
                    .toList();

            return paginationUtils.convertListToPage(page, perPage, list);
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public CompletableFuture<TypeBonusResponse> getTypeBonusById(UUID typeBonusId) {
        var typeBonusFuture = findTypeBonusByID(typeBonusId);

        return typeBonusFuture.thenApply(typeBonusMapper::toTypeBonusResponse);
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public CompletableFuture<TypeBonusResponse> updateTypeBonusById(UUID typeBonusId, TypeBonusUpdateRequest request) {
        var typeBonusFuture = findTypeBonusByID(typeBonusId);

        return typeBonusFuture.thenCompose(updateTypeBonus -> {
            if (updateTypeBonus.getQuantity() != request.getQuantity()) {
                var foundTypeBonus = typeBonusRepository.findAll().stream()
                        .filter(typeBonus -> typeBonus.getName().equalsIgnoreCase(updateTypeBonus.getName()))
                        .filter(typeBonus -> typeBonus.getQuantity() == request.getQuantity())
                        .findFirst();
                if (foundTypeBonus.isPresent()) {
                    throw new AppException(ErrorCode.TYPE_BONUS_ALREADY_EXIST);
                }
            }
            typeBonusMapper.updateTypeBonus(updateTypeBonus, request);

            return CompletableFuture
                    .completedFuture(typeBonusMapper.toTypeBonusResponse(typeBonusRepository.save(updateTypeBonus)));
        });
    }

    @Async("AsyncTaskExecutor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public CompletableFuture<TypeBonusResponse> deleteTypeBonus(UUID typeBonusId) {
        var typeBonusFuture = findTypeBonusByID(typeBonusId);

        return typeBonusFuture.thenApply(typeBonus -> {
            typeBonusRepository.delete(typeBonus);

            return typeBonusMapper.toTypeBonusResponse(typeBonus);
        });
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<TypeBonus> findByNameAndQuantity(String name, long quantity) {
        return typeBonusRepository.findByNameAndQuantity(name, quantity);
    }

    @Async("AsyncTaskExecutor")
    private CompletableFuture<TypeBonus> findTypeBonusByID(UUID typeBonusId) {
        return CompletableFuture.supplyAsync(() ->
                typeBonusRepository.findById(typeBonusId)
                        .orElseThrow(() -> new AppException(ErrorCode.TYPE_BONUS_NOT_FOUND)));
    }
}
