package com.example.adoptr_backend.repository;

import com.example.adoptr_backend.model.Province;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProvinceRepository extends JpaRepository<Province, Long> {
}
