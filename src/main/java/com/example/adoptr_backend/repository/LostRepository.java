package com.example.adoptr_backend.repository;

import com.example.adoptr_backend.model.Lost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface LostRepository  extends JpaRepository<Lost, Long>, JpaSpecificationExecutor<Lost>{
    List<Lost> findByIdIn(List<Long> ids);
}

