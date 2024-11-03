package com.example.adoptr_backend.controller;

import com.example.adoptr_backend.service.StatisticsService;
import com.example.adoptr_backend.service.dto.response.ReportSummaryDTO;
import com.example.adoptr_backend.service.dto.response.SummaryDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/statistics")
@Tag(name = "Statistics", description = "Statistics Endpoints")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Obtiene las estadisticas de adopter", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<SummaryDTO> getStatistics() {
        SummaryDTO response = statisticsService.getSummary();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/report/summary")
    @Operation(summary = "Obtiene estadísticas de reportes", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<ReportSummaryDTO> getReportStats() {
        ReportSummaryDTO response = statisticsService.getReportSummary();
        return ResponseEntity.ok(response);
    }
}
