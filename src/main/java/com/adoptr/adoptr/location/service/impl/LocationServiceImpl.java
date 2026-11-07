package com.adoptr.adoptr.location.service.impl;

import com.adoptr.adoptr.location.dto.response.LocalityDTO;
import com.adoptr.adoptr.location.dto.response.ProvinceDTO;
import com.adoptr.adoptr.location.error.LocationError;
import com.adoptr.adoptr.location.mapper.LocationMapper;
import com.adoptr.adoptr.location.repository.LocalityRepository;
import com.adoptr.adoptr.location.repository.ProvinceRepository;
import com.adoptr.adoptr.location.service.LocationService;
import com.adoptr.adoptr.shared.exception.custom.BadRequestException;
import com.adoptr.adoptr.shared.exception.custom.NotFoundException;
import com.adoptr.adoptr.shared.exception.error.CommonError;
import com.adoptr.adoptr.shared.util.SearchText;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationServiceImpl implements LocationService {

    static final int MIN_QUERY_LENGTH = 2;
    static final int MAX_QUERY_LENGTH = 100;
    static final int MAX_RESULTS = 20;
    static final String INVALID_QUERY_MESSAGE =
            "La búsqueda de localidades necesita entre " + MIN_QUERY_LENGTH + " y " + MAX_QUERY_LENGTH + " caracteres";

    private final ProvinceRepository provinceRepository;
    private final LocalityRepository localityRepository;

    public LocationServiceImpl(ProvinceRepository provinceRepository, LocalityRepository localityRepository) {
        this.provinceRepository = provinceRepository;
        this.localityRepository = localityRepository;
    }

    @Override
    public List<ProvinceDTO> getProvinces() {
        return LocationMapper.MAPPER.toProvinceDtos(provinceRepository.findAllByOrderByNameAsc());
    }

    @Override
    public List<LocalityDTO> searchLocalities(Long provinceId, String query) {
        String text = SearchText.normalize(query);
        if (text.length() < MIN_QUERY_LENGTH || text.length() > MAX_QUERY_LENGTH) {
            throw new BadRequestException(CommonError.VALIDATION_ERROR.getCode(), INVALID_QUERY_MESSAGE);
        }
        if (!provinceRepository.existsById(provinceId)) {
            throw new NotFoundException(LocationError.PROVINCE_NOT_FOUND);
        }
        String escaped = text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return LocationMapper.MAPPER.toLocalityDtos(
                localityRepository.search(provinceId, escaped, PageRequest.of(0, MAX_RESULTS)));
    }
}
