package com.adoptr.adoptr.adoption.service.impl;

import com.adoptr.adoptr.adoption.dto.response.AdoptionSummaryDTO;
import com.adoptr.adoptr.adoption.mapper.AdoptionMapper;
import com.adoptr.adoptr.adoption.model.AdoptionPublication;
import com.adoptr.adoptr.publication.repository.PublicationPhotoRepository;
import com.adoptr.adoptr.shared.dto.PageDTO;
import com.adoptr.adoptr.shared.storage.ImageStorage;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AdoptionSummaries {

    private final PublicationPhotoRepository photoRepository;
    private final ImageStorage imageStorage;
    private final Clock clock;

    public AdoptionSummaries(PublicationPhotoRepository photoRepository, ImageStorage imageStorage, Clock clock) {
        this.photoRepository = photoRepository;
        this.imageStorage = imageStorage;
        this.clock = clock;
    }

    public PageDTO<AdoptionSummaryDTO> page(Page<AdoptionPublication> page) {
        YearMonth current = YearMonth.now(clock);
        Map<Long, String> covers = coverUrls(page.getContent());
        return PageDTO.from(page, publication -> {
            AdoptionSummaryDTO dto = AdoptionMapper.toSummary(publication, current);
            dto.setCoverPhotoUrl(covers.get(publication.getId()));
            return dto;
        });
    }

    private Map<Long, String> coverUrls(List<AdoptionPublication> publications) {
        if (publications.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = publications.stream().map(AdoptionPublication::getId).toList();
        return photoRepository.findByPublicationIdInOrderBySortOrderAsc(ids).stream()
                .collect(Collectors.toMap(photo -> photo.getPublication().getId(),
                        photo -> imageStorage.publicUrl(photo.getS3Key()),
                        (cover, other) -> cover));
    }
}
