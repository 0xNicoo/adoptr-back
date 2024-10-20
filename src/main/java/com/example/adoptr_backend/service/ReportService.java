package com.example.adoptr_backend.service;

import com.example.adoptr_backend.model.ReportModelType;
import com.example.adoptr_backend.service.dto.request.ReportDTOin;
import com.example.adoptr_backend.service.dto.response.ReportDTO;

import java.util.List;

public interface ReportService {
    <T extends ReportDTO> List<T> getReports(ReportModelType reportModelType);

    void report(ReportModelType reportModelType, ReportDTOin dto);
}
