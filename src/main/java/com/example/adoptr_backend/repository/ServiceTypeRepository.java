package com.example.adoptr_backend.repository;

import com.example.adoptr_backend.model.ServiceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceTypeRepository extends JpaRepository<ServiceType, Long> {
    Page<ServiceType> findAll(Specification<ServiceType> spec, Pageable pageable);
}
