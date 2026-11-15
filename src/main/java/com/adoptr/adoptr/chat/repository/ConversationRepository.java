package com.adoptr.adoptr.chat.repository;

import com.adoptr.adoptr.chat.model.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByPublicationIdAndInterestedId(Long publicationId, Long interestedId);

    /** Con la publicación ya cargada (con su subtipo real, no un proxy de la clase abstracta). */
    @Query("select c from Conversation c join fetch c.publication where c.id = :id")
    Optional<Conversation> findWithPublicationById(@Param("id") Long id);

    /**
     * Bandeja de un usuario, de la conversación con actividad más reciente a la más vieja.
     * {@code asOwner} incluye las de sus publicaciones (lo contactaron); {@code asInterested}, las que inició.
     */
    @Query(value = """
            select c from Conversation c
            join fetch c.publication p
            where ((:asOwner = true and c.owner.id = :userId)
                   or (:asInterested = true and c.interested.id = :userId))
              and (:publicationId is null or p.id = :publicationId)
            order by c.lastMessageAt desc, c.id desc
            """,
            countQuery = """
            select count(c) from Conversation c
            where ((:asOwner = true and c.owner.id = :userId)
                   or (:asInterested = true and c.interested.id = :userId))
              and (:publicationId is null or c.publication.id = :publicationId)
            """)
    Page<Conversation> findForUser(@Param("userId") Long userId,
                                   @Param("asOwner") boolean asOwner,
                                   @Param("asInterested") boolean asInterested,
                                   @Param("publicationId") Long publicationId,
                                   Pageable pageable);
}
