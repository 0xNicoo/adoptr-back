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
import com.example.adoptr_backend.service.dto.response.*;
import com.example.adoptr_backend.service.mapper.ReportMapper;
import com.example.adoptr_backend.service.mapper.UserMapper;
import com.example.adoptr_backend.util.AuthSupport;
import com.example.adoptr_backend.util.ReportSupport;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class PublicationReportStrategy implements ReportStrategy{

    private final ReportRepository reportRepository;

    private final ReportReasonRepository reportReasonRepository;

    private final PublicationRepository publicationRepository;


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
    public <T extends ReportDetailDTO> List<T> getReports() {
        List<Report> reports = reportRepository.findAllByModelType(this.getType());

        List<Publication> uniquePublicationList = getPublications(reports);

        Map<Long, List<Report>> reportsByPublication = reports.stream()
                .collect(Collectors.groupingBy(Report::getModelId));

        List<PublicationReportDTO> reportDTOList = uniquePublicationList.stream()
                .map(pub -> getPublicationReportDTO(pub, reportsByPublication))
                .toList();
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

    /**
     * Obtiene la lista de publicaciones, sin repetir ninguna publicacion.
     * @param reports
     * @return
     */
    private List<Publication> getPublications(List<Report> reports) {
        List<Long> uniquePublicationsIds = reports.stream()
                .map(Report::getModelId)
                .distinct()
                .toList();
        return publicationRepository.findByIdIn(uniquePublicationsIds);
    }

    private PublicationDTO getPublicationDTO(Publication pub) {
        PublicationDTO publicationDTO = new PublicationDTO();
        publicationDTO.setId(pub.getId());
        publicationDTO.setTitle(pub.getTitle());
        UserDTO userDTO = UserMapper.MAPPER.toDto(pub.getUser());
        publicationDTO.setUser(userDTO);
        return publicationDTO;
    }

    private PublicationReportDTO getPublicationReportDTO(Publication pub, Map<Long, List<Report>> reportsByPublication) {
        PublicationReportDTO dto = new PublicationReportDTO();
        List<Report> relatedReports = reportsByPublication.get(pub.getId());

        if(relatedReports != null){
            dto.setReportCount((long) relatedReports.size());
            dto.setUrl(ReportSupport.buildPublicationURL(pub));
            List<ReportDTO> reportDTOs = relatedReports.stream().map(ReportMapper.MAPPER::toDto).toList();
            dto.setReports(reportDTOs);
            PublicationDTO publicationDTO = getPublicationDTO(pub);
            dto.setPublication(publicationDTO);
        }
        return dto;
    }
}
