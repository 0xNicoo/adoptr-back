package com.example.adoptr_backend.repository;

import com.example.adoptr_backend.model.Donation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    Page<Donation> findAll(Specification<Donation> spec, Pageable pageable);
}
