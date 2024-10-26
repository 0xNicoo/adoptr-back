package com.example.adoptr_backend.repository;

import com.example.adoptr_backend.model.Report;
import com.example.adoptr_backend.model.ReportModelType;
import lombok.extern.java.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findAllByModelType(@Param("modelType") ReportModelType modelType);

    List<Report> findAllByModelTypeAndModelId(@Param("modelType") ReportModelType modelType,
                                              @Param("modelId") Long modelId);

    Optional<Report> findByModelTypeAndModelIdAndReporterUserId(@Param("modelType") ReportModelType modelType,
                                                                @Param("modelId") Long modelId,
                                                                @Param("reporterUserId") Long reporterUserId);

    long countByModelType(ReportModelType reportModelType);

}
