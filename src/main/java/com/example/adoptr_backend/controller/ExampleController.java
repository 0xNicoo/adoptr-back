package com.example.adoptr_backend.controller;

import com.example.adoptr_backend.service.ExampleService;
import com.example.adoptr_backend.service.dto.request.ExampleDTOin;
import com.example.adoptr_backend.service.dto.response.ExampleDTO;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/example")
public class ExampleController {

    private final ExampleService exampleService;

    public ExampleController(ExampleService exampleService) {
        this.exampleService = exampleService;
    }

    @PostMapping
    public ResponseEntity<ExampleDTO> create(@RequestBody ExampleDTOin dto){
        ExampleDTO response =  exampleService.create(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ExampleDTO>> getAll(){
        List<ExampleDTO> response = exampleService.getAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExampleDTO> getById(@PathVariable Long id) throws BadRequestException {
        ExampleDTO response = exampleService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExampleDTO> update(@PathVariable Long id, @RequestBody ExampleDTOin dto) throws BadRequestException {
        ExampleDTO response = exampleService.update(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) throws BadRequestException {
        exampleService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
