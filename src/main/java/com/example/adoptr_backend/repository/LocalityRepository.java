package com.example.adoptr_backend.repository;

import com.example.adoptr_backend.model.Locality;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalityRepository extends JpaRepository<Locality, Long> {
}
