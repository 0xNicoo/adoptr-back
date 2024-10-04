package com.example.adoptr_backend.repository;
import com.example.adoptr_backend.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface PostRepository  extends JpaRepository<Post, Long> {
    List<Post> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);
    Page<Post> findAllByOrderByDateDesc(Pageable pageable);

}
