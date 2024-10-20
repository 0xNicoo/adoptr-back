package com.example.adoptr_backend.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/report")
@Tag(name = "Report", description = "Report Endpoints")
public class ReportController {

    @GetMapping("/publication")
    @Operation(summary = "Obtiene todas las publicaciones reportadas", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<String> getAll(){
        return ResponseEntity.ok("");
    }

    @PatchMapping("/publication/{id}")
    @Operation(summary = "Reporta una publicacion", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<String> report(@PathVariable Long id){
        return ResponseEntity.ok("");
    }

}
