package com.example.adoptr_backend.service.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProfileReportDTO extends ReportDetailDTO{
    private ProfileDTO profileDTO;
    private String url;
}
