package com.example.adoptr_backend.controller;

import com.example.adoptr_backend.service.ServicesService;
import com.example.adoptr_backend.service.dto.request.ServiceDTOin;
import com.example.adoptr_backend.service.dto.response.ServiceDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/service")
@Tag(name = "Service", description = "Service Endpoints")
public class ServiceController {

    private final ServicesService servicesService;

    public ServiceController(ServicesService servicesService) {this.servicesService = servicesService;}

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "crea una publicacion de servicio", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<ServiceDTO> create(@RequestParam String title,
                                             @RequestParam String description,
                                             @RequestParam String street,
                                             @RequestParam int number,
                                             @RequestParam MultipartFile image,
                                             @RequestParam Long locality_id,
                                             @RequestParam Long serviceType_id){
        ServiceDTOin dto = new ServiceDTOin(title, description, street, number, image, locality_id, serviceType_id);
        ServiceDTO response =  servicesService.create(dto);
        return ResponseEntity.ok(response);
    }
}