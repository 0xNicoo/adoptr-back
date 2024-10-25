package com.example.adoptr_backend.service.impl.report;

import com.example.adoptr_backend.exception.custom.BadRequestException;
import com.example.adoptr_backend.exception.error.Error;
import com.example.adoptr_backend.model.Profile;
import com.example.adoptr_backend.model.Report;
import com.example.adoptr_backend.model.ReportModelType;
import com.example.adoptr_backend.model.ReportReason;
import com.example.adoptr_backend.repository.ProfileRepository;
import com.example.adoptr_backend.repository.ReportReasonRepository;
import com.example.adoptr_backend.repository.ReportRepository;
import com.example.adoptr_backend.service.dto.request.ReportDTOin;
import com.example.adoptr_backend.service.dto.response.*;
import com.example.adoptr_backend.service.mapper.ReportMapper;
import com.example.adoptr_backend.service.mapper.UserMapper;
import com.example.adoptr_backend.util.AuthSupport;
import com.example.adoptr_backend.util.ReportSupport;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class ProfileReportStrategy implements ReportStrategy{

    private final ReportRepository reportRepository;

    private final ReportReasonRepository reportReasonRepository;

    private final ProfileRepository profileRepository;

    @Override
    public void report(ReportDTOin dto) {
        Long reporterUserId = AuthSupport.getUserId();
        Profile profile = getProfile(dto.getModelId());
        if(Objects.equals(profile.getUser().getId(), reporterUserId)){
            throw new BadRequestException(Error.USER_CAN_NOT_REPORT_PROFILE);
        }
        verifyIfUserAlreadyReport(profile.getId(), reporterUserId);
        Report report = createReport(profile.getId(), dto.getReasonId(), reporterUserId);
        reportRepository.save(report);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends ReportDetailDTO> List<T> getReports() {
        List<Report> reports = reportRepository.findAllByModelType(this.getType());

        List<Profile> uniqueProfileList = getProfiles(reports);

        Map<Long, List<Report>> reportsByProfile = reports.stream()
                .collect(Collectors.groupingBy(Report::getModelId));

        List<ProfileReportDTO> reportDTOList = uniqueProfileList.stream()
                .map(pub -> getProfileReportDTO(pub, reportsByProfile))
                .toList();
        return (List<T>) reportDTOList;
    }

    @Override
    public List<ReportDTO> getReportByModelId(Long modelId) {
        return null;
    }

    @Override
    public ReportModelType getType() {
        return ReportModelType.PROFILE;
    }

    private void verifyIfUserAlreadyReport(Long profileId, Long reporterUserId){
        Optional<Report> reportOptional = reportRepository.findByModelTypeAndModelIdAndReporterUserId(
                ReportModelType.PROFILE,
                profileId,
                reporterUserId
        );
        if(reportOptional.isPresent()){
            throw new BadRequestException(Error.USER_ALREADY_REPORTED_PROFILE);
        }
    }

    private Report createReport(Long profileId, Long reasonId, Long reporterUserId){
        Report report = new Report();
        report.setModelType(ReportModelType.PROFILE);
        report.setModelId(profileId);
        report.setReporterUserId(reporterUserId);
        Optional<ReportReason> reasonOptional = reportReasonRepository.findById(reasonId);
        reasonOptional.ifPresent(report::setReason);
        return report;
    }

    private Profile getProfile(Long profileId){
        Optional<Profile> profileOptional = profileRepository.findById(profileId);
        if(profileOptional.isEmpty()){
            throw new BadRequestException(Error.PROFILE_NOT_FOUND);
        }
        return profileOptional.get();
    }

    /**
     * Obtiene la lista de perfiles, sin repetir ningun perfil.
     * @param reports
     * @return
     */
    private List<Profile> getProfiles(List<Report> reports) {
        List<Long> uniqueProfilesIds = reports.stream()
               .map(Report::getModelId)
               .distinct()
                .toList();
       return profileRepository.findAllById(uniqueProfilesIds);
    }

    private ProfileDTO getProfileDTO(Profile prof) {
        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setId(prof.getId());
        profileDTO.setFirstName(prof.getFirstName());
        profileDTO.setLastName(prof.getLastName());
        profileDTO.setDescription(prof.getDescription());
        profileDTO.setGenderType(prof.getGenderType());
        UserDTO userDTO = UserMapper.MAPPER.toDto(prof.getUser());
        profileDTO.setUser(userDTO);
        return profileDTO;
    }

    private ProfileReportDTO getProfileReportDTO(Profile prof, Map<Long, List<Report>> reportsByProfile) {
        ProfileReportDTO dto = new ProfileReportDTO();
        List<Report> relatedReports = reportsByProfile.get(prof.getId());

        if(relatedReports != null){
            dto.setReportCount((long) relatedReports.size());
            dto.setUrl(ReportSupport.buildProfileURL(prof.getUser().getId()));
            List<ReportDTO> reportDTOs = relatedReports.stream().map(ReportMapper.MAPPER::toDto).toList();
            dto.setReports(reportDTOs);
            ProfileDTO profileDTO = getProfileDTO(prof);
            dto.setProfile(profileDTO);
        }
        return dto;
    }
}
