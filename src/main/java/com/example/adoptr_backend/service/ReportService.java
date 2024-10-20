package com.example.adoptr_backend.service;

import com.example.adoptr_backend.model.ReportModelType;
import com.example.adoptr_backend.service.dto.request.ReportDTOin;

import java.util.List;

public interface ReportService {
    <T> List<T> getReports(ReportModelType reportModelType);

    void report(ReportModelType reportModelType, ReportDTOin dto);
}
