package com.example.adoptr_backend.repository;

import com.example.adoptr_backend.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByAdopterUserIdOrPublicationUserId(@Param("adopterUserId") Long adopterUserId, @Param("publicationUserId") Long publicationUserId);

    @Query("SELECT c FROM Chat c WHERE c.publication.id = :publicationId AND (c.adopterUserId = :userId OR c.publicationUserId = :userId)")
    List<Chat> findByPublicationIdAndUserId(@Param("publicationId") Long publicationId, @Param("userId") Long userId);

    @Query("SELECT COUNT(c) > 0 FROM Chat c " +
            "WHERE c.publication.id = :publicationId " +
            "AND ((c.adopterUserId = :adopterUserId AND c.publicationUserId = :publicationUserId) " +
            "OR (c.adopterUserId = :publicationUserId AND c.publicationUserId = :adopterUserId))")

    boolean existsChatForPublicationBetweenUsers(
            @Param("publicationId") Long publicationId,
            @Param("adopterUserId") Long adopterUserId,
            @Param("publicationUserId") Long publicationUserId
    );
}
