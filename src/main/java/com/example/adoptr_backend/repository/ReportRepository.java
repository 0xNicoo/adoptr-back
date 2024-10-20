package com.example.adoptr_backend.repository;

import com.example.adoptr_backend.model.Report;
import com.example.adoptr_backend.model.ReportModelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Optional<Report> findByModelTypeAndModelIdAndReporterUserId(@Param("modelType") ReportModelType modelType,
                                                                @Param("modelId") Long modelId,
                                                                @Param("reporterUserId") Long reporterUserId);
}
