package com.example.adoptr_backend.service.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PostReportDTO extends ReportDetailDTO{
    private PostDTO post;
    private String url;
}
