package com.example.adoptr_backend.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportSummaryDTO {
    private Long totalPublicationsReported;
    private Long adoptionReported;
    private Long lostReported;
    private Long serviceReported;
    private Long postReported;
    private Long profileReported;
}
