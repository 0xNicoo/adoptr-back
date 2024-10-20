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
import com.example.adoptr_backend.service.dto.response.PublicationDTO;
import com.example.adoptr_backend.service.dto.response.PublicationReportDTO;
import com.example.adoptr_backend.service.dto.response.ReportDTO;
import com.example.adoptr_backend.util.AuthSupport;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PublicationReportStrategy implements ReportStrategy{

    private final ReportRepository reportRepository;

    private final ReportReasonRepository reportReasonRepository;

    private final PublicationRepository publicationRepository;

    public PublicationReportStrategy(ReportRepository reportRepository,
                                     ReportReasonRepository reportReasonRepository,
                                     PublicationRepository publicationRepository) {
        this.reportRepository = reportRepository;
        this.reportReasonRepository = reportReasonRepository;
        this.publicationRepository = publicationRepository;

    }

    @Override
    public void report(ReportDTOin dto) {
        Long reporterUserId = AuthSupport.getUserId();
        Publication publication = getPublication(dto.getModelId());
        if(Objects.equals(publication.getUser().getId(), reporterUserId)){
            throw new BadRequestException(Error.USER_CAN_NOT_REPORT_PUBLICATION);
        }
        verifyIfUserAlreadyReport(publication.getId(), reporterUserId);
        Report report = createReport(publication.getId(), dto.getReasonId(), reporterUserId);
        reportRepository.save(report);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends ReportDTO> List<T> getReports() {
        List<Report> reports = reportRepository.findAll();

        List<Long> uniquePublicationsIds = reports.stream()
                .map(Report::getModelId)
                .distinct()
                .toList();
        Map<Long, List<Report>> reportsByPublication = reports.stream()
                .collect(Collectors.groupingBy(Report::getModelId));

        List<Publication> publicationList = publicationRepository.findByIdIn(uniquePublicationsIds);
        Map<Long, Publication> publicationMap = publicationList.stream()
                .collect(Collectors.toMap(Publication::getId, publication -> publication));

        List<PublicationReportDTO> reportDTOList = reports.stream().map(report -> {
            PublicationReportDTO dto = new PublicationReportDTO();
            dto.setId(report.getId());
            dto.setModelType(report.getModelType());

            Publication publication = publicationMap.get(report.getModelId());
            if(publication != null){
                PublicationDTO publicationDTO = new PublicationDTO();
                publicationDTO.setId(publication.getId());
                publicationDTO.setTitle(publication.getTitle());
                dto.setPublicationDTO(publicationDTO);
            }
            return dto;
        }).toList();
        return (List<T>) reportDTOList;
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

    private Publication getPublication(Long publicationId){
        Optional<Publication> publicationOptional = publicationRepository.findById(publicationId);
        if(publicationOptional.isEmpty()){
            throw new BadRequestException(Error.PUBLICATION_NOT_FOUND);
        }
        return publicationOptional.get();
    }
}
