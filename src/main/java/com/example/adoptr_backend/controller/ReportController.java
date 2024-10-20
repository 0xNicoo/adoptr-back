package com.example.adoptr_backend.controller;


import com.example.adoptr_backend.model.ReportModelType;
import com.example.adoptr_backend.service.ReportService;
import com.example.adoptr_backend.service.dto.request.ReportDTOin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/report")
@Tag(name = "Report", description = "Report Endpoints")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/publication")
    @Operation(summary = "Obtiene todas las publicaciones reportadas", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<String> getAll(){
        return ResponseEntity.ok("");
    }

    @PostMapping("/publication")
    @Operation(summary = "Reporta una publicacion", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<String> report(@RequestBody ReportDTOin dto){
        reportService.report(ReportModelType.PUBLICATION, dto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
