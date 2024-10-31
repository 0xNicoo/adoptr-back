package com.example.adoptr_backend.repository;

import com.example.adoptr_backend.model.Found;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FoundRepository extends JpaRepository<Found, Long>{
    @Modifying
    @Transactional
    @Query("DELETE FROM Found f WHERE f.lost.id = :lostId")
    void deleteByLostId(@Param("lostId") Long lostId);
}
