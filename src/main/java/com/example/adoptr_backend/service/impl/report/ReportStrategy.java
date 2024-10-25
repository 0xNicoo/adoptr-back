package com.example.adoptr_backend.service.impl.report;

import com.example.adoptr_backend.model.ReportModelType;
import com.example.adoptr_backend.service.dto.request.ReportDTOin;
import com.example.adoptr_backend.service.dto.response.ReportDTO;
import com.example.adoptr_backend.service.dto.response.ReportDetailDTO;

import java.util.List;

public interface ReportStrategy {

    void report(ReportDTOin dto);

    <T extends ReportDetailDTO> List<T> getReports();

    /**
     * Obtiene los reportes de una: Publicacion, Perfil, Post.
     * @param modelId
     * @return
     */
    List<ReportDTO> getReportByModelId(Long modelId);

    ReportModelType getType();
}
