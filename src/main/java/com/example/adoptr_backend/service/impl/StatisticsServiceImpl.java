package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.model.*;
import com.example.adoptr_backend.repository.*;
import com.example.adoptr_backend.service.StatisticsService;
import com.example.adoptr_backend.service.dto.response.ReportSummaryDTO;
import com.example.adoptr_backend.service.dto.response.SummaryDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final PublicationRepository publicationRepository;
    private final ProfileRepository profileRepository;
    private final PostRepository postRepository;
    private final AdoptionRepository adoptionRepository;
    private final ServiceRepository serviceRepository;
    private final LostRepository lostRepository;
    private final ReportRepository reportRepository;


    @Transactional
    @Override
    public SummaryDTO getSummary() {
        Long publicationsCount = publicationRepository.count();
        Long profilesCount = profileRepository.count();
        Long postCount = postRepository.count();
        Long adoptionPublicationsCount = adoptionRepository.countByAdoptionStatusType(AdoptionStatusType.FOR_ADOPTION);
        Long adoptedPetsCount = adoptionRepository.countByAdoptionStatusType(AdoptionStatusType.ADOPTED);
        Long servicePublicationsCount = serviceRepository.count();
        Long lostPublicationsCount = lostRepository.count();
        return new SummaryDTO(
                publicationsCount,
                profilesCount,
                postCount,
                adoptionPublicationsCount,
                adoptedPetsCount,
                servicePublicationsCount,
                lostPublicationsCount
        );
    }

    @Override
    public ReportSummaryDTO getReportSummary() {
        //TODO: Hacer todo esto con un @Query en el repository, para no traer todos los reports a memoria.
        List<Report> publicationReports = reportRepository.findAllByModelType(ReportModelType.PUBLICATION);
        long adoptionReports = countReportsByPublicationType(publicationReports, PublicationType.ADOPTION);
        long lostReports = countReportsByPublicationType(publicationReports, PublicationType.LOST);
        long serviceReports = countReportsByPublicationType(publicationReports, PublicationType.SERVICE);
        long postReports = reportRepository.countByModelType(ReportModelType.POST);
        long profileReports = reportRepository.countByModelType(ReportModelType.PROFILE);

        return new ReportSummaryDTO(
                (long) publicationReports.size(),
                adoptionReports,
                lostReports,
                serviceReports,
                postReports,
                profileReports
        );
    }

    private long countReportsByPublicationType(List<Report> reports, PublicationType type) {
        return reports.stream()
                .map(Report::getModelId)
                .distinct()
                .filter(publicationId -> {
                    Publication publication = publicationRepository.findById(publicationId)
                            .orElse(null);
                    return publication != null && publication.getType() == type;
                })
                .count();
    }
}
