package com.example.adoptr_backend.repository;

import com.example.adoptr_backend.model.Publication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublicationRepository extends JpaRepository<Publication, Long> {
    List<Publication> findByIdIn(List<Long> ids);
}
