package com.example.adoptr_backend.service;

import com.example.adoptr_backend.model.Publication;

import java.util.Optional;

public interface PublicationService {

    Publication get(Long id);

    Long getPublicationCount();

}
