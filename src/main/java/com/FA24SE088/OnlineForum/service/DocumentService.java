package com.FA24SE088.OnlineForum.service;

import com.FA24SE088.OnlineForum.dto.request.ImageSectionRequest;
import com.FA24SE088.OnlineForum.dto.request.SectionRequest;
import com.FA24SE088.OnlineForum.dto.request.DocumentRequest;
import com.FA24SE088.OnlineForum.dto.request.VideoSectionRequest;
import com.FA24SE088.OnlineForum.dto.response.ImageSectionResponse;
import com.FA24SE088.OnlineForum.dto.response.SectionResponse;
import com.FA24SE088.OnlineForum.dto.response.DocumentResponse;
import com.FA24SE088.OnlineForum.dto.response.VideoSectionResponse;
import com.FA24SE088.OnlineForum.entity.*;
import com.FA24SE088.OnlineForum.enums.DocumentStatus;
import com.FA24SE088.OnlineForum.exception.AppException;
import com.FA24SE088.OnlineForum.exception.ErrorCode;
import com.FA24SE088.OnlineForum.mapper.SectionMapper;
import com.FA24SE088.OnlineForum.mapper.DocumentMapper;
import com.FA24SE088.OnlineForum.repository.Repository.ImageSectionRepository;
import com.FA24SE088.OnlineForum.repository.Repository.SectionRepository;
import com.FA24SE088.OnlineForum.repository.Repository.DocumentRepository;
import com.FA24SE088.OnlineForum.repository.Repository.VideoSectionRepository;
import com.FA24SE088.OnlineForum.repository.UnitOfWork.UnitOfWork;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@Service
public class DocumentService {

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    ImageSectionRepository imageSectionRepository;

    @Autowired
    VideoSectionRepository videoSectionRepository;
    @Autowired
    UnitOfWork unitOfWork;

    @Autowired
    DocumentMapper documentMapper;

    @Autowired
    SectionMapper sectionMapper;

    @Transactional
    public DocumentResponse createSourceCode(DocumentRequest request) {
        // Bước 1: Tạo đối tượng SourceCode từ request
        Document document = new Document();
        document.setName(request.getName());
        document.setImage(request.getImage());
        document.setPrice(request.getPrice());
        document.setType(request.getType());
        document.setStatus(request.getStatus());

        // Lưu SourceCode vào database trước
        document = documentRepository.save(document);

        List<SectionResponse> sectionResponses = new ArrayList<>();

        // Bước 2: Xử lý tạo Section cùng các đối tượng liên quan
        for (SectionRequest sectionRequest : request.getSectionList()) {
            Section section = new Section();
            section.setCreatedDate(new Date()); // Đặt ngày hiện tại
            section.setLinkGit(sectionRequest.getLinkGit());
            section.setDocument(document); // Liên kết với SourceCode vừa tạo

            // Lưu Section vào database
            section = sectionRepository.save(section);

            List<ImageSectionResponse> imageResponses = new ArrayList<>();
            for (ImageSectionRequest imageRequest : sectionRequest.getImageSectionList()) {
                ImageSection imageSection = new ImageSection();
                imageSection.setUrl(imageRequest.getUrl());
                imageSection.setSection(section);

                // Lưu ImageSection vào database
                imageSection = imageSectionRepository.save(imageSection);

                // Tạo phản hồi ImageSectionResponse
                imageResponses.add(new ImageSectionResponse(imageSection.getUrl()));
            }

            List<VideoSectionResponse> videoResponses = new ArrayList<>();
            for (VideoSectionRequest videoRequest : sectionRequest.getVideoSectionList()) {
                VideoSection videoSection = new VideoSection();
                videoSection.setUrl(videoRequest.getUrl());
                videoSection.setSection(section);

                // Lưu VideoSection vào database
                videoSection = videoSectionRepository.save(videoSection);

                // Tạo phản hồi VideoSectionResponse
                videoResponses.add(new VideoSectionResponse(videoSection.getUrl()));
            }

            // Tạo phản hồi SectionResponse
            SectionResponse sectionResponse = new SectionResponse();
            sectionResponse.setLinkGit(section.getLinkGit());
            sectionResponse.setImageSectionList(imageResponses);
            sectionResponse.setVideoSectionList(videoResponses);

            sectionResponses.add(sectionResponse);
        }

        // Tạo phản hồi SourceCodeResponse
        DocumentResponse response = new DocumentResponse();
        response.setName(document.getName());
        response.setImage(document.getImage());
        response.setPrice(document.getPrice());
        response.setType(document.getType());
        response.setStatus(document.getStatus());
        response.setSectionList(sectionResponses);

        // Trả về đối tượng SourceCodeResponse
        return response;
    }

    @Transactional
    public DocumentResponse createDocument(DocumentRequest request) {
        if (!request.getStatus().equals(DocumentStatus.ACTIVE.name()) &&
                !request.getStatus().equals(DocumentStatus.INACTIVE.name())){
            throw new AppException(ErrorCode.WRONG_STATUS);
        }
            Document document = documentMapper.toSourceCode(request);

        document = unitOfWork.getDocumentRepository().save(document);
        List<SectionResponse> sectionResponses = new ArrayList<>();

        for (SectionRequest sectionRequest : request.getSectionList()) {
            Section section = sectionMapper.toSection(sectionRequest);
            section.setCreatedDate(new Date());
            section.setDocument(document);

            section = unitOfWork.getSectionRepository().save(section);


            List<ImageSectionResponse> imageResponses = new ArrayList<>();
            for (ImageSectionRequest imageRequest : sectionRequest.getImageSectionList()) {
                ImageSection imageSection = new ImageSection();
                imageSection.setUrl(imageRequest.getUrl());
                imageSection.setSection(section);

                imageSection = unitOfWork.getImageSectionRepository().save(imageSection);

                imageResponses.add(new ImageSectionResponse(imageSection.getUrl()));
            }

            List<VideoSectionResponse> videoResponses = new ArrayList<>();
            for (VideoSectionRequest videoRequest : sectionRequest.getVideoSectionList()) {
                VideoSection videoSection = new VideoSection();
                videoSection.setUrl(videoRequest.getUrl());
                videoSection.setSection(section);

                videoSection = unitOfWork.getVideoSectionRepository().save(videoSection);

                videoResponses.add(new VideoSectionResponse(videoSection.getUrl()));
            }

            SectionResponse sectionResponse = new SectionResponse();
            sectionResponse.setLinkGit(section.getLinkGit());
            sectionResponse.setImageSectionList(imageResponses);
            sectionResponse.setVideoSectionList(videoResponses);

            sectionResponses.add(sectionResponse);
        }

        DocumentResponse response = documentMapper.toResponse(document);
        response.setSectionList(sectionResponses);

        return response;
    }

    @Transactional
    public DocumentResponse update(UUID documentID, DocumentRequest request) {
        if (!request.getStatus().equals(DocumentStatus.ACTIVE.name()) &&
                !request.getStatus().equals(DocumentStatus.INACTIVE.name())){
            throw new AppException(ErrorCode.WRONG_STATUS);
        }
        Document document = unitOfWork.getDocumentRepository()
                .findById(documentID)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));

        document.setName(request.getName());
        document.setImage(request.getImage());
        document.setPrice(request.getPrice());
        document.setType(request.getType());
        document.setStatus(request.getStatus());

        // Xóa các Section cũ liên quan đến document này
        unitOfWork.getSectionRepository().deleteAllByDocument(document);

        List<SectionResponse> sectionResponses = new ArrayList<>();

        for (SectionRequest sectionRequest : request.getSectionList()) {
            Section section = sectionMapper.toSection(sectionRequest);
            section.setDocument(document); // Gán section vào document

            section = unitOfWork.getSectionRepository().save(section);

            List<ImageSectionResponse> imageResponses = new ArrayList<>();
            for (ImageSectionRequest imageRequest : sectionRequest.getImageSectionList()) {
                ImageSection imageSection = new ImageSection();
                imageSection.setUrl(imageRequest.getUrl());
                imageSection.setSection(section);

                imageSection = unitOfWork.getImageSectionRepository().save(imageSection);

                imageResponses.add(new ImageSectionResponse(imageSection.getUrl()));
            }

            List<VideoSectionResponse> videoResponses = new ArrayList<>();
            for (VideoSectionRequest videoRequest : sectionRequest.getVideoSectionList()) {
                VideoSection videoSection = new VideoSection();
                videoSection.setUrl(videoRequest.getUrl());
                videoSection.setSection(section);

                videoSection = unitOfWork.getVideoSectionRepository().save(videoSection);

                videoResponses.add(new VideoSectionResponse(videoSection.getUrl()));
            }

            SectionResponse sectionResponse = new SectionResponse();
            sectionResponse.setLinkGit(section.getLinkGit());
            sectionResponse.setImageSectionList(imageResponses);
            sectionResponse.setVideoSectionList(videoResponses);

            sectionResponses.add(sectionResponse);
        }

        unitOfWork.getDocumentRepository().save(document);

        DocumentResponse response = documentMapper.toResponse(document);
        response.setSectionList(sectionResponses);

        return response;
    }



    public List<DocumentResponse> getAll() {
        return documentRepository.findAll().stream()
                .map(document -> {
                    DocumentResponse response = documentMapper.toResponse(document);
                    List<SectionResponse> sectionResponses = document.getSectionList().stream()
                            .map(documentMapper::toSectionResponse)
                            .collect(Collectors.toList());
                    response.setSectionList(sectionResponses);

                    return response;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteDocument(UUID documentId) {
        Document document = unitOfWork.getDocumentRepository()
                .findById(documentId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));
        document.getRedeemList().forEach(redeem -> {
            if(redeem.getDocument().getDocumentId().equals(documentId))
                throw new AppException(ErrorCode.DOCUMENT_HAS_BEEN_USED);
        });
        unitOfWork.getDocumentRepository().delete(document);
    }


}

