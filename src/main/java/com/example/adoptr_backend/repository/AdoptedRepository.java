package com.example.adoptr_backend.repository;

import com.example.adoptr_backend.model.Adopted;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AdoptedRepository extends JpaRepository<Adopted, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM Adopted a WHERE a.adoption.id = :adoptionId")
    void deleteByAdoptionId(@Param("adoptionId") Long adoptionId);
}
