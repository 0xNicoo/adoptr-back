package com.example.adoptr_backend.repository;

import com.example.adoptr_backend.model.Example;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExampleRepository extends JpaRepository<Example, Long> {
}
