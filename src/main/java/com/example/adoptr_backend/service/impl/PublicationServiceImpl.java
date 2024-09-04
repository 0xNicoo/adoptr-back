package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.exception.custom.BadRequestException;
import com.example.adoptr_backend.exception.error.Error;
import com.example.adoptr_backend.model.Adoption;
import com.example.adoptr_backend.model.Publication;
import com.example.adoptr_backend.repository.PublicationRepository;
import com.example.adoptr_backend.service.PublicationService;
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
            throw new BadRequestException(Error.PUBLICATION_NOT_FOUND);
        }
        Publication publication = publicationOptional.get();

        //TODO mejorar esto cuando esten los 3 tipos de publicacion
        if(publication instanceof Adoption){
            return publication;
        }

        throw new BadRequestException(Error.PUBLICATION_NOT_FOUND);
    }
}
