package com.example.adoptr_backend.service.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class ReportDetailDTO {
    private List<ReportDTO> reports;
    private Long reportCount;
}
