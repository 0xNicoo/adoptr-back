package com.example.adoptr_backend.service.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PublicationReportDTO extends ReportDetailDTO{
    private PublicationDTO publication;
    private String url;
}
