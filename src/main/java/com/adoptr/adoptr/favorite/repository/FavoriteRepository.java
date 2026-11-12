package com.adoptr.adoptr.favorite.repository;

import com.adoptr.adoptr.adoption.model.AdoptionPublication;
import com.adoptr.adoptr.adoption.model.Species;
import com.adoptr.adoptr.favorite.model.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserIdAndPublicationId(Long userId, Long publicationId);

    @Modifying
    @Transactional
    @Query("delete from Favorite f where f.user.id = :userId and f.publication.id = :publicationId")
    int deleteByUserIdAndPublicationId(@Param("userId") Long userId, @Param("publicationId") Long publicationId);

    @Query(value = """
            select a from Favorite f
            join AdoptionPublication a on a.id = f.publication.id
            join fetch a.locality l
            join fetch l.province
            where f.user.id = :userId
              and a.deletedAt is null
              and (:species is null or a.species = :species)
            order by f.createdAt desc, f.id desc
            """,
            countQuery = """
            select count(f) from Favorite f
            join AdoptionPublication a on a.id = f.publication.id
            where f.user.id = :userId
              and a.deletedAt is null
              and (:species is null or a.species = :species)
            """)
    Page<AdoptionPublication> findAdoptions(@Param("userId") Long userId, @Param("species") Species species,
                                            Pageable pageable);

    @Query("""
            select f.publication.id from Favorite f
            where f.user.id = :userId and f.publication.deletedAt is null
            order by f.createdAt desc, f.id desc
            """)
    List<Long> findPublicationIds(@Param("userId") Long userId);
}
