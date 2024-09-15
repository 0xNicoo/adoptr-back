package com.example.adoptr_backend.repository;
import com.example.adoptr_backend.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
public interface PostRepository  extends JpaRepository<Post, Long> {
    List<Post> findByUserId(Long userId);
}
