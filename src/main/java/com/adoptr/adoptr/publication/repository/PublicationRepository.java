package com.adoptr.adoptr.publication.repository;

import com.adoptr.adoptr.publication.model.Publication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublicationRepository extends JpaRepository<Publication, Long> {
    List<Publication> findByIdIn(List<Long> ids);
}
