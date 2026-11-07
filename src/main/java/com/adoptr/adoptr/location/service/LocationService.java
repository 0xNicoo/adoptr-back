package com.adoptr.adoptr.location.service;

import com.adoptr.adoptr.location.dto.response.LocalityDTO;
import com.adoptr.adoptr.location.dto.response.ProvinceDTO;

import java.util.List;

public interface LocationService {
    List<ProvinceDTO> getProvinces();

    List<LocalityDTO> searchLocalities(Long provinceId, String query);
}
