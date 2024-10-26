package com.example.adoptr_backend.service;

import com.example.adoptr_backend.model.ReportModelType;
import com.example.adoptr_backend.model.ReportReason;
import com.example.adoptr_backend.service.dto.request.ReportDTOin;
import com.example.adoptr_backend.service.dto.response.ReportDTO;
import com.example.adoptr_backend.service.dto.response.ReportDetailDTO;
import com.example.adoptr_backend.service.dto.response.ReportStatsDTO;

import java.util.List;
import java.util.Map;

public interface ReportService {
    <T extends ReportDetailDTO> List<T> getReports(ReportModelType reportModelType);

    void report(ReportModelType reportModelType, ReportDTOin dto);

    List<ReportReason> getReportReasons(ReportModelType reportModelType);

    List<ReportDTO> getReportsByModelId(ReportModelType reportModelType, Long modelId);

    ReportStatsDTO getReportStats();
}
