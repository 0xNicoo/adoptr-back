package com.example.adoptr_backend.controller;

import com.example.adoptr_backend.service.ExampleService;
import com.example.adoptr_backend.service.dto.request.ExampleDTOin;
import com.example.adoptr_backend.service.dto.request.ExampleFilterDTO;
import com.example.adoptr_backend.service.dto.response.ExampleDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.coyote.BadRequestException;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/example")
@Tag(name = "Example", description = "Examples Endpoints")
public class ExampleController {

    private final ExampleService exampleService;

    public ExampleController(ExampleService exampleService) {
        this.exampleService = exampleService;
    }

    @PostMapping
    @Operation(summary = "Crea un ejemplo", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<ExampleDTO> create(@RequestBody ExampleDTOin dto){
        ExampleDTO response =  exampleService.create(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Obtiene ejemplos, con filtro", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<List<ExampleDTO>> getAll(@ParameterObject ExampleFilterDTO filterDTO, @ParameterObject Pageable pageable){
        Page<ExampleDTO> response = exampleService.getAll(filterDTO, pageable);
        return new ResponseEntity<>(response.getContent(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un ejemplo por id", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<ExampleDTO> getById(@PathVariable Long id) throws BadRequestException {
        ExampleDTO response = exampleService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifica un ejemplo", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<ExampleDTO> update(@PathVariable Long id, @RequestBody ExampleDTOin dto) throws BadRequestException {
        ExampleDTO response = exampleService.update(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un ejemplo", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<String> delete(@PathVariable Long id) throws BadRequestException {
        exampleService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
