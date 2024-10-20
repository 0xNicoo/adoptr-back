package com.example.adoptr_backend.service.impl.report;

import com.example.adoptr_backend.exception.custom.BadRequestException;
import com.example.adoptr_backend.exception.error.Error;
import com.example.adoptr_backend.model.Publication;
import com.example.adoptr_backend.model.Report;
import com.example.adoptr_backend.model.ReportModelType;
import com.example.adoptr_backend.model.ReportReason;
import com.example.adoptr_backend.repository.PublicationRepository;
import com.example.adoptr_backend.repository.ReportReasonRepository;
import com.example.adoptr_backend.repository.ReportRepository;
import com.example.adoptr_backend.service.PublicationService;
import com.example.adoptr_backend.service.dto.request.ReportDTOin;
import com.example.adoptr_backend.util.AuthSupport;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class PublicationReportStrategy implements ReportStrategy{

    private final ReportRepository reportRepository;

    private final ReportReasonRepository reportReasonRepository;

    private final PublicationService publicationService;

    public PublicationReportStrategy(ReportRepository reportRepository,
                                     ReportReasonRepository reportReasonRepository,
                                     PublicationService publicationService) {
        this.reportRepository = reportRepository;
        this.reportReasonRepository = reportReasonRepository;
        this.publicationService = publicationService;

    }

    @Override
    public void report(ReportDTOin dto) {
        Long reporterUserId = AuthSupport.getUserId();
        Publication publication = publicationService.get(dto.getModelId());
        if(Objects.equals(publication.getUser().getId(), reporterUserId)){
            throw new BadRequestException(Error.USER_CAN_NOT_REPORT_PUBLICATION);
        }
        verifyIfUserAlreadyReport(publication.getId(), reporterUserId);
        Report report = createReport(publication.getId(), dto.getReasonId(), reporterUserId);
        reportRepository.save(report);
    }

    @Override
    public ReportModelType getType() {
        return ReportModelType.PUBLICATION;
    }

    private void verifyIfUserAlreadyReport(Long publicationId, Long reporterUserId){
        Optional<Report> reportOptional = reportRepository.findByModelTypeAndModelIdAndReporterUserId(
                ReportModelType.PUBLICATION,
                publicationId,
                reporterUserId
        );
        if(reportOptional.isPresent()){
            throw new BadRequestException(Error.USER_ALREADY_REPORTED_PUBLICATION);
        }
    }

    private Report createReport(Long publicationId, Long reasonId, Long reporterUserId){
        Report report = new Report();
        report.setModelType(ReportModelType.PUBLICATION);
        report.setModelId(publicationId);
        report.setReporterUserId(reporterUserId);
        Optional<ReportReason> reasonOptional = reportReasonRepository.findById(reasonId);
        reasonOptional.ifPresent(report::setReason);
        return report;
    }
}
