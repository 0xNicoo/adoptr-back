package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.exception.custom.BadRequestException;
import com.example.adoptr_backend.exception.error.type.ExampleError;
import com.example.adoptr_backend.model.Example;
import com.example.adoptr_backend.repository.ExampleRepository;
import com.example.adoptr_backend.repository.specification.ExampleSpec;
import com.example.adoptr_backend.service.ExampleService;
import com.example.adoptr_backend.service.dto.request.ExampleDTOin;
import com.example.adoptr_backend.service.dto.request.ExampleFilterDTO;
import com.example.adoptr_backend.service.dto.response.ExampleDTO;
import com.example.adoptr_backend.service.mapper.ExampleMapper;
import com.example.adoptr_backend.util.AuthSupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ExampleServiceImpl implements ExampleService {

    private final ExampleRepository exampleRepository;

    public ExampleServiceImpl(ExampleRepository exampleRepository) {
        this.exampleRepository = exampleRepository;
    }

    @Override
    public ExampleDTO create(ExampleDTOin dto) {
        Long userId = AuthSupport.getUserId();
        Example example = ExampleMapper.MAPPER.toEntity(dto);
        example.setCreatedByUser(userId);
        example = exampleRepository.save(example);
        return ExampleMapper.MAPPER.toDto(example);
    }

    @Override
    public ExampleDTO getById(Long id) throws BadRequestException {
        Example example = getExample(id);
        return ExampleMapper.MAPPER.toDto(example);
    }

    @Override
    public Page<ExampleDTO> getAll(ExampleFilterDTO filter, Pageable pageable) {
        Specification<Example> spec = ExampleSpec.getSpec(filter);
        Page<Example> page = exampleRepository.findAll(spec, pageable);
        return page.map(ExampleMapper.MAPPER::toDto);
    }

    @Override
    public ExampleDTO update(Long id, ExampleDTOin dto) throws BadRequestException {
        Example example = getExample(id);
        Example exampleUpdated = ExampleMapper.MAPPER.toEntity(dto);
        ExampleMapper.MAPPER.update(example, exampleUpdated);
        exampleRepository.save(example);
        return ExampleMapper.MAPPER.toDto(example);
    }

    @Override
    public void delete(Long id) throws BadRequestException {
        Example example = getExample(id);
        exampleRepository.delete(example);
    }

    private Example getExample(Long id) throws BadRequestException {
        Optional<Example> exampleOptional = exampleRepository.findById(id);
        if(exampleOptional.isEmpty()){
            throw new BadRequestException(ExampleError.EXAMPLE_NOT_FOUND);
        }
        return exampleOptional.get();
    }
}
