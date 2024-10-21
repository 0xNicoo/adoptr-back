package com.example.adoptr_backend.service.dto.response;

import com.example.adoptr_backend.model.ReportModelType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportDTO {
    private Long id;
    private Long modelId;
    private Long reporterUserId;
    private ReportModelType modelType;
    private ReportReasonDTO reason;
    private LocalDateTime createdAt;
}
