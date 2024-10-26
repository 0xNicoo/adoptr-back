package com.example.adoptr_backend.repository;
import com.example.adoptr_backend.model.Likes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikesRepository extends JpaRepository<Likes, Long> {
    List<Likes> findByUserId(Long userId);

    List<Likes> findByPostId(Long postId);

    Optional<Likes> findByUser_IdAndPost_Id(Long userId, Long postId);

    Long countByPostId(Long postId);
}
