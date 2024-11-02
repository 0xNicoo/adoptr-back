package com.example.adoptr_backend.controller;

import com.example.adoptr_backend.service.DonationService;
import com.example.adoptr_backend.service.ProvinceService;
import com.example.adoptr_backend.service.dto.request.DonationDTOin;
import com.example.adoptr_backend.service.dto.request.DonationFilterDTO;
import com.example.adoptr_backend.service.dto.request.ProvinceDTOin;
import com.example.adoptr_backend.service.dto.request.ProvinceFilterDTO;
import com.example.adoptr_backend.service.dto.response.DonationDTO;
import com.example.adoptr_backend.service.dto.response.ProvinceDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/donation")
@Tag(name = "Donation", description = "Donation Endpoints")
public class DonationController {

    private final DonationService donationService;

    public DonationController(DonationService donationService) {
        this.donationService = donationService;
    }

    @PostMapping
    @Operation(summary = "Crea una donación")
    public ResponseEntity<DonationDTO> create(@RequestBody DonationDTOin dto){
        DonationDTO response =  donationService.create(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Obtiene donaciones, con filtro")
    public ResponseEntity<List<DonationDTO>> getAll(@ParameterObject DonationFilterDTO filterDTO, @ParameterObject Pageable pageable){
        Page<DonationDTO> response = donationService.getAll(filterDTO, pageable);
        return new ResponseEntity<>(response.getContent(), HttpStatus.OK);
    }
}
