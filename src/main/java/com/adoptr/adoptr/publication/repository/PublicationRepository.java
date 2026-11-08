package com.adoptr.adoptr.publication.repository;

import com.adoptr.adoptr.publication.model.Publication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicationRepository extends JpaRepository<Publication, Long> {
}
