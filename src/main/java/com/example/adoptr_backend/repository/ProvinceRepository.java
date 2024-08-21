package com.example.adoptr_backend.repository;

import com.example.adoptr_backend.model.Province;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProvinceRepository extends JpaRepository<Province, Long> {
    Page<Province> findAll(Specification<Province> spec, Pageable pageable);
}
