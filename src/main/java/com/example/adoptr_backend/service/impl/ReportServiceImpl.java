package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.model.*;
import com.example.adoptr_backend.repository.PublicationRepository;
import com.example.adoptr_backend.repository.ReportReasonRepository;
import com.example.adoptr_backend.repository.ReportRepository;
import com.example.adoptr_backend.service.ReportService;
import com.example.adoptr_backend.service.dto.request.ReportDTOin;
import com.example.adoptr_backend.service.dto.response.ReportDTO;
import com.example.adoptr_backend.service.dto.response.ReportDetailDTO;
import com.example.adoptr_backend.service.dto.response.ReportStatsDTO;
import com.example.adoptr_backend.service.impl.report.ReportStrategy;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    private final List<ReportStrategy> reportStrategies;

    private Map<ReportModelType, ReportStrategy> reportStrategyMap;

    private final ReportReasonRepository reportReasonRepository;

    private final ReportRepository reportRepository;

    private final PublicationRepository publicationRepository;

    public ReportServiceImpl(
            List<ReportStrategy> reportStrategies,
            ReportReasonRepository reportReasonRepository,
            ReportRepository reportRepository,
            PublicationRepository publicationRepository){
        this.reportStrategies = reportStrategies;
        this.reportReasonRepository = reportReasonRepository;
        this.reportRepository = reportRepository;
        this.publicationRepository = publicationRepository;
    }

    @PostConstruct
    public void setStrategyMap(){
        this.reportStrategyMap = new HashMap<>();
        reportStrategies.forEach(reportStrategy -> reportStrategyMap.put(reportStrategy.getType(), reportStrategy));
    }

    @Override
    public <T extends ReportDetailDTO> List<T> getReports(ReportModelType reportModelType) {
        return reportStrategyMap.get(reportModelType).getReports();
    }

    @Override
    public void report(ReportModelType reportModelType, ReportDTOin dto) {
        reportStrategyMap.get(reportModelType).report(dto);
    }

    @Override
    public List<ReportReason> getReportReasons(ReportModelType reportModelType) {
        return reportReasonRepository.findAllByModelType(reportModelType);
    }

    @Override
    public List<ReportDTO> getReportsByModelId(ReportModelType reportModelType, Long modelId) {
        return reportStrategyMap.get(reportModelType).getReportByModelId(modelId);
    }

    @Override
    public ReportStatsDTO getReportStats() {
        List<Report> publicationReports = reportRepository.findAllByModelType(ReportModelType.PUBLICATION);
        long adoptionReports = countReportsByPublicationType(publicationReports, PublicationType.ADOPTION);
        long lostReports = countReportsByPublicationType(publicationReports, PublicationType.LOST);
        long serviceReports = countReportsByPublicationType(publicationReports, PublicationType.SERVICE);
        long postReports = reportRepository.countByModelType(ReportModelType.POST);
        long profileReports = reportRepository.countByModelType(ReportModelType.PROFILE);

        return new ReportStatsDTO(
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
