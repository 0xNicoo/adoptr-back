package com.adoptr.adoptr.publication.service.impl;


import com.adoptr.adoptr.publication.model.Publication;
import com.adoptr.adoptr.publication.repository.PublicationRepository;
import com.adoptr.adoptr.publication.service.PublicationService;
import com.adoptr.adoptr.shared.exception.custom.BadRequestException;
import com.adoptr.adoptr.publication.error.PublicationError;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class PublicationServiceImpl implements PublicationService {
    private final PublicationRepository publicationRepository;

    @Override
    public Publication get(Long id) {
        Optional<Publication> publicationOptional = publicationRepository.findById(id);
        if(publicationOptional.isEmpty()){
            throw new BadRequestException(PublicationError.NOT_FOUND);
        }
        return publicationOptional.get();
    }
}
