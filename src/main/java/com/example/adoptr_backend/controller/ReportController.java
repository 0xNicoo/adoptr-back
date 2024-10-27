package com.example.adoptr_backend.controller;


import com.example.adoptr_backend.model.ReportModelType;
import com.example.adoptr_backend.model.ReportReason;
import com.example.adoptr_backend.service.ReportService;
import com.example.adoptr_backend.service.dto.request.ReportDTOin;
import com.example.adoptr_backend.service.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/report")
@Tag(name = "Report", description = "Report Endpoints")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/reasons")
    @Operation(summary = "Obtiene razones de reporte", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<List<ReportReason>> getReportReasons(@RequestParam ReportModelType reportModelType){
        List<ReportReason> response = reportService.getReportReasons(reportModelType);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/publication")
    @Operation(summary = "Obtiene todas las publicaciones reportadas", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<List<PublicationReportDTO>> getAll(){
        List<PublicationReportDTO> response = reportService.getReports(ReportModelType.PUBLICATION);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/publication/{id}")
    @Operation(summary = "Obtiene todos los reportes de una publicacion", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<List<ReportDTO>> getReportsByModelId(@PathVariable Long id){
        List<ReportDTO> response = reportService.getReportsByModelId(ReportModelType.PUBLICATION, id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/publication")
    @Operation(summary = "Reporta una publicacion", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<String> report(@RequestBody ReportDTOin dto){
        reportService.report(ReportModelType.PUBLICATION, dto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/profile")
    @Operation(summary = "Obtiene todos los perfiles reportados", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<List<ProfileReportDTO>> getAllProfiles(){
        List<ProfileReportDTO> response = reportService.getReports(ReportModelType.PROFILE);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile/{id}")
    @Operation(summary = "Obtiene todos los reportes de un perfil", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<List<ReportDTO>> getReportsByModelIdProfile(@PathVariable Long id){
        List<ReportDTO> response = reportService.getReportsByModelId(ReportModelType.PROFILE, id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/profile")
    @Operation(summary = "Reporta un perfil", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<String> reportProfile(@RequestBody ReportDTOin dto){
        reportService.report(ReportModelType.PROFILE, dto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/post")
    @Operation(summary = "Obtiene todos los post reportados", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<List<PostReportDTO>> getAllPost(){
        List<PostReportDTO> response = reportService.getReports(ReportModelType.POST);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/post")
    @Operation(summary = "Reporta una post", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<String> reportPost(@RequestBody ReportDTOin dto){
        reportService.report(ReportModelType.POST, dto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/post/{id}")
    @Operation(summary = "Obtiene todos los reportes de un post", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<List<ReportDTO>> getReportsByModelIdPost(@PathVariable Long id){
        List<ReportDTO> response = reportService.getReportsByModelId(ReportModelType.POST, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    @Operation(summary = "Obtiene estadísticas de reportes", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<ReportStatsDTO> getReportStats() {
        ReportStatsDTO stats = reportService.getReportStats();
        return ResponseEntity.ok(stats);
    }

}
