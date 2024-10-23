package com.example.adoptr_backend.controller;


import com.example.adoptr_backend.model.ReportModelType;
import com.example.adoptr_backend.model.ReportReason;
import com.example.adoptr_backend.service.ReportService;
import com.example.adoptr_backend.service.dto.request.ReportDTOin;
import com.example.adoptr_backend.service.dto.response.PostReportDTO;
import com.example.adoptr_backend.service.dto.response.ProfileReportDTO;
import com.example.adoptr_backend.service.dto.response.PublicationReportDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<ReportReason>> getReportReasons(){
        List<ReportReason> response = reportService.getReportReasons();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/publication")
    @Operation(summary = "Obtiene todas las publicaciones reportadas", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<List<PublicationReportDTO>> getAll(){
        List<PublicationReportDTO> response = reportService.getReports(ReportModelType.PUBLICATION);
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

}
