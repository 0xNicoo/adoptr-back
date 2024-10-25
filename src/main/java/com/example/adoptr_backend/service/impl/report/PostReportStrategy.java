package com.example.adoptr_backend.service.impl.report;

import com.example.adoptr_backend.exception.custom.BadRequestException;
import com.example.adoptr_backend.exception.error.Error;
import com.example.adoptr_backend.model.*;
import com.example.adoptr_backend.repository.PostRepository;
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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class PostReportStrategy implements ReportStrategy{

    private final ReportRepository reportRepository;

    private final ReportReasonRepository reportReasonRepository;

    private final PostRepository postRepository;

    @Override
    public void report(ReportDTOin dto) {
        Long reporterUserId = AuthSupport.getUserId();
        Post post = getPost(dto.getModelId());
        if(Objects.equals(post.getUser().getId(), reporterUserId)){
            throw new BadRequestException(Error.USER_CAN_NOT_REPORT_POST);
        }
        verifyIfUserAlreadyReport(post.getId(), reporterUserId);
        Report report = createReport(post.getId(), dto.getReasonId(), reporterUserId);
        reportRepository.save(report);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends ReportDetailDTO> List<T> getReports() {
        List<Report> reports = reportRepository.findAllByModelType(this.getType());

        List<Post> uniquePostList = getPosts(reports);

        Map<Long, List<Report>> reportsByPost = reports.stream()
                .collect(Collectors.groupingBy(Report::getModelId));

        List<PostReportDTO> reportDTOList = uniquePostList.stream()
                .map(pos -> getPostReportDTO(pos, reportsByPost))
                .toList();
        return (List<T>) reportDTOList;
    }

    @Override
    public List<ReportDTO> getReportByModelId(Long modelId) {
        return null;
    }

    @Override
    public ReportModelType getType() {
        return ReportModelType.POST;
    }

    private void verifyIfUserAlreadyReport(Long postId, Long reporterUserId){
        Optional<Report> reportOptional = reportRepository.findByModelTypeAndModelIdAndReporterUserId(
                ReportModelType.POST,
                postId,
                reporterUserId
        );
        if(reportOptional.isPresent()){
            throw new BadRequestException(Error.USER_ALREADY_REPORTED_POST);
        }
    }

    private Report createReport(Long postId, Long reasonId, Long reporterUserId){
        Report report = new Report();
        report.setModelType(ReportModelType.POST);
        report.setModelId(postId);
        report.setReporterUserId(reporterUserId);
        Optional<ReportReason> reasonOptional = reportReasonRepository.findById(reasonId);
        reasonOptional.ifPresent(report::setReason);
        return report;
    }

    private Post getPost(Long postId){
        Optional<Post> postOptional = postRepository.findById(postId);
        if(postOptional.isEmpty()){
            throw new BadRequestException(Error.POST_NOT_FOUND);
        }
        return postOptional.get();
    }

    /**
     * Obtiene la lista de post, sin repetir ningun post.
     * @param reports
     * @return
     */
    private List<Post> getPosts(List<Report> reports) {
        List<Long> uniquePostsIds = reports.stream()
                .map(Report::getModelId)
                .distinct()
                .toList();
        return postRepository.findByIdIn(uniquePostsIds);
    }

    private PostDTO getPostDTO(Post pos) {
        PostDTO postDTO = new PostDTO();
        postDTO.setId(pos.getId());
        postDTO.setDescription(pos.getDescription());
        UserDTO userDTO = UserMapper.MAPPER.toDto(pos.getUser());
        postDTO.setUser(userDTO);
        return postDTO;
    }

    private PostReportDTO getPostReportDTO(Post pos, Map<Long, List<Report>> reportsByPost) {
        PostReportDTO dto = new PostReportDTO();
        List<Report> relatedReports = reportsByPost.get(pos.getId());

        if(relatedReports != null){
            dto.setReportCount((long) relatedReports.size());
            dto.setUrl(ReportSupport.buildPostURL(pos));
            List<ReportDTO> reportDTOs = relatedReports.stream().map(ReportMapper.MAPPER::toDto).toList();
            dto.setReports(reportDTOs);
            PostDTO postDTO = getPostDTO(pos);
            dto.setPost(postDTO);
        }
        return dto;
    }
}
