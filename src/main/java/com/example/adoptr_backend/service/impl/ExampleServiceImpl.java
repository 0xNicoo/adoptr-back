package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.model.Example;
import com.example.adoptr_backend.repository.ExampleRepository;
import com.example.adoptr_backend.service.ExampleService;
import com.example.adoptr_backend.service.dto.request.ExampleDTOin;
import com.example.adoptr_backend.service.dto.response.ExampleDTO;
import com.example.adoptr_backend.service.mapper.ExampleMapper;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExampleServiceImpl implements ExampleService {

    private final ExampleRepository exampleRepository;

    public ExampleServiceImpl(ExampleRepository exampleRepository) {
        this.exampleRepository = exampleRepository;
    }

    @Override
    public ExampleDTO create(ExampleDTOin dto) {
        Example example = ExampleMapper.MAPPER.toEntity(dto);
        example = exampleRepository.save(example);
        return ExampleMapper.MAPPER.toDto(example);
    }

    @Override
    public ExampleDTO getById(Long id) throws BadRequestException {
        Example example = getExample(id);
        return ExampleMapper.MAPPER.toDto(example);
    }

    @Override
    public List<ExampleDTO> getAll() {
        List<Example> examples = exampleRepository.findAll();
        return ExampleMapper.MAPPER.toDto(examples);
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
            throw new BadRequestException();
        }
        return exampleOptional.get();
    }
}
