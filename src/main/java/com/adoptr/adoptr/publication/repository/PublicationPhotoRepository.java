package com.adoptr.adoptr.publication.repository;

import com.adoptr.adoptr.publication.model.PublicationPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PublicationPhotoRepository extends JpaRepository<PublicationPhoto, Long> {

    List<PublicationPhoto> findByPublicationIdOrderBySortOrderAsc(Long publicationId);

    /** Para los listados: trae las fotos de toda la página en una sola consulta. */
    List<PublicationPhoto> findByPublicationIdInOrderBySortOrderAsc(Collection<Long> publicationIds);

    Optional<PublicationPhoto> findByIdAndPublicationId(Long id, Long publicationId);
}
