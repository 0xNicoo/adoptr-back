package com.adoptr.adoptr.adoption.repository;

import com.adoptr.adoptr.adoption.model.AdoptionPublication;
import com.adoptr.adoptr.adoption.model.AdoptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AdoptionPublicationRepository extends JpaRepository<AdoptionPublication, Long>,
        JpaSpecificationExecutor<AdoptionPublication> {

    /** La publicación si existe y no fue dada de baja. */
    Optional<AdoptionPublication> findByIdAndDeletedAtIsNull(Long id);

    /** Publicaciones activas de un autor, en cualquier estado, con localidad y provincia en la misma consulta. */
    @EntityGraph(attributePaths = {"locality", "locality.province"})
    Page<AdoptionPublication> findByAuthorIdAndDeletedAtIsNull(Long authorId, Pageable pageable);

    /** Publicaciones activas de un autor en un estado, con localidad y provincia en la misma consulta. */
    @EntityGraph(attributePaths = {"locality", "locality.province"})
    Page<AdoptionPublication> findByAuthorIdAndStatusAndDeletedAtIsNull(Long authorId, AdoptionStatus status,
                                                                        Pageable pageable);
}
