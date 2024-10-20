package com.example.adoptr_backend.service.impl.report;

import com.example.adoptr_backend.model.ReportModelType;
import com.example.adoptr_backend.service.dto.request.ReportDTOin;

public interface ReportStrategy {

    void report(ReportDTOin dto);

    ReportModelType getType();
}
