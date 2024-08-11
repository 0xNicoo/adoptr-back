package com.example.adoptr_backend.repository;

import com.example.adoptr_backend.model.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;

public interface ExampleRepository extends JpaRepository<Example, Long>, JpaSpecificationExecutor<Example> {
}
