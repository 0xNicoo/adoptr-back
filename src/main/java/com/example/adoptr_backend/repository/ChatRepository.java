package com.example.adoptr_backend.repository;

import com.example.adoptr_backend.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByAdopterUserIdOrPublicationUserId(@Param("adopterUserId") Long adopterUserId, @Param("publicationUserId") Long publicationUserId);

    Optional<Chat> findByPublicationIdAndAdopterUserId(@Param("publicationId") Long publicationId, @Param("adopterUserId") Long adopterUserId);
}
