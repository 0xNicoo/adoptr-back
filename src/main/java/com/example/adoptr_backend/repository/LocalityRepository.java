package com.example.adoptr_backend.repository;

import com.example.adoptr_backend.model.Locality;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalityRepository extends JpaRepository<Locality, Long> {
    Page<Locality> findAll(Specification<Locality> spec, Pageable pageable);
}
