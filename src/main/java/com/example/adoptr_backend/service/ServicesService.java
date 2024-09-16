package com.example.adoptr_backend.service;

import com.example.adoptr_backend.service.dto.request.ServiceDTOin;
import com.example.adoptr_backend.service.dto.response.ServiceDTO;

public interface ServicesService {
    ServiceDTO create(ServiceDTOin dto);
}
