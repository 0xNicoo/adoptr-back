package com.example.adoptr_backend.service;

import com.example.adoptr_backend.service.dto.request.ExampleDTOin;
import com.example.adoptr_backend.service.dto.request.ExampleFilterDTO;
import com.example.adoptr_backend.service.dto.response.ExampleDTO;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ExampleService {

    ExampleDTO create(ExampleDTOin dto);

    ExampleDTO getById(Long id) throws BadRequestException;

    Page<ExampleDTO> getAll(ExampleFilterDTO filter, Pageable pageable);

    ExampleDTO update(Long id, ExampleDTOin dto) throws BadRequestException;

    void delete(Long id) throws BadRequestException;
}
