package com.example.adoptr_backend.service;

import com.example.adoptr_backend.service.dto.request.ExampleDTOin;
import com.example.adoptr_backend.service.dto.response.ExampleDTO;
import org.apache.coyote.BadRequestException;

import java.util.List;

public interface ExampleService {

    ExampleDTO create(ExampleDTOin dto);

    ExampleDTO getById(Long id) throws BadRequestException;

    List<ExampleDTO> getAll();

    ExampleDTO update(Long id, ExampleDTOin dto) throws BadRequestException;

    void delete(Long id) throws BadRequestException;
}
