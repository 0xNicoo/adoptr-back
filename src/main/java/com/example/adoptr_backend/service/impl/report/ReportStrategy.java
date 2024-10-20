package com.example.adoptr_backend.service.impl.report;

import com.example.adoptr_backend.model.ReportModelType;
import com.example.adoptr_backend.service.dto.request.ReportDTOin;
import com.example.adoptr_backend.service.dto.response.ReportDTO;

import java.util.List;

public interface ReportStrategy {

    void report(ReportDTOin dto);

    <T extends ReportDTO> List<T> getReports();

    ReportModelType getType();
}
