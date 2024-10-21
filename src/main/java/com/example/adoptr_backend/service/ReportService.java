package com.example.adoptr_backend.service;

import com.example.adoptr_backend.model.ReportModelType;
import com.example.adoptr_backend.service.dto.request.ReportDTOin;
import com.example.adoptr_backend.service.dto.response.ReportDTO;
import com.example.adoptr_backend.service.dto.response.ReportDetailDTO;

import java.util.List;

public interface ReportService {
    <T extends ReportDetailDTO> List<T> getReports(ReportModelType reportModelType);

    void report(ReportModelType reportModelType, ReportDTOin dto);
}
