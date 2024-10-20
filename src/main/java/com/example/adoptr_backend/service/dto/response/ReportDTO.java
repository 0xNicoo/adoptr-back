package com.example.adoptr_backend.service.dto.response;

import com.example.adoptr_backend.model.ReportModelType;
import lombok.Data;

@Data
public class ReportDTO {
    private Long id;
    private String url;
    private Long reports;
    private ReportModelType modelType;
}
