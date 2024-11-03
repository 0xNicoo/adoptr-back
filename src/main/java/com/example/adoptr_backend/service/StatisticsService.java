package com.example.adoptr_backend.service;

import com.example.adoptr_backend.service.dto.response.ReportSummaryDTO;
import com.example.adoptr_backend.service.dto.response.SummaryDTO;

public interface StatisticsService {
    SummaryDTO getSummary();

    ReportSummaryDTO getReportSummary();
}
