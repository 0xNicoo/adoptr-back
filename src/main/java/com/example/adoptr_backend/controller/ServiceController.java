package com.example.adoptr_backend.controller;

import com.example.adoptr_backend.model.AnimalType;
import com.example.adoptr_backend.model.SexType;
import com.example.adoptr_backend.model.SizeType;
import com.example.adoptr_backend.service.ServicesService;
import com.example.adoptr_backend.service.dto.request.AdoptionDTOin;
import com.example.adoptr_backend.service.dto.request.ServiceDTOin;
import com.example.adoptr_backend.service.dto.request.ServiceFilterDTO;
import com.example.adoptr_backend.service.dto.response.AdoptionDTO;
import com.example.adoptr_backend.service.dto.response.ServiceDTO;
import com.example.adoptr_backend.util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @GetMapping
    @Operation(summary = "Obtiene publicaciones de servicio, con filtro")
    public ResponseEntity<List<ServiceDTO>> getAll(@ParameterObject ServiceFilterDTO filterDTO, @ParameterObject Pageable pageable){
        Page<ServiceDTO> response = servicesService.getAll(filterDTO, pageable);
        HttpHeaders headers = PaginationUtil.setTotalCountPageHttpHeaders(response);
        return new ResponseEntity<>(response.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una publicacion de servicio por id")
    public ResponseEntity<ServiceDTO> getById(@PathVariable Long id) {
        ServiceDTO response = servicesService.getById(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina una publicacion de servicio", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<String> delete(@PathVariable Long id)  {
        servicesService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Modifica una publicacion de servicio", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<ServiceDTO> update(@PathVariable Long id,
                                             @RequestParam String title,
                                             @RequestParam String description,
                                             @RequestParam String street,
                                             @RequestParam int number,
                                             @RequestParam(required = false) MultipartFile image,
                                             @RequestParam Long locality_id,
                                             @RequestParam Long serviceType_id){
        ServiceDTOin dto = new ServiceDTOin(title, description, street, number, image, locality_id, serviceType_id);
        ServiceDTO response = servicesService.update(id, dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Obtiene publicaciones de servicio por usuario", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<List<ServiceDTO>> getByUserId(@PathVariable Long userId, @ParameterObject Pageable pageable) {
        Page<ServiceDTO> response = servicesService.getByUserId(userId, pageable);
        HttpHeaders headers = PaginationUtil.setTotalCountPageHttpHeaders(response);
        return new ResponseEntity<>(response.getContent(), headers, HttpStatus.OK);
    }

}
