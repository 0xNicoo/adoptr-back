package com.example.adoptr_backend.repository;

import com.example.adoptr_backend.model.Adoption;
import com.example.adoptr_backend.model.AdoptionStatusType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AdoptionRepository extends JpaRepository<Adoption, Long>, JpaSpecificationExecutor<Adoption> {
    List<Adoption> findByIdIn(List<Long> ids);

    Long countByAdoptionStatusType(AdoptionStatusType adoptionStatusType);
}
