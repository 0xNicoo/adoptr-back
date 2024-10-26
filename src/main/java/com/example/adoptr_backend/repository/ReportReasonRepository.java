package com.example.adoptr_backend.repository;

import com.example.adoptr_backend.model.ReportModelType;
import com.example.adoptr_backend.model.ReportReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportReasonRepository extends JpaRepository<ReportReason, Long> {
    List<ReportReason> findAllByModelType(@Param("modelType") ReportModelType modelType);
}
