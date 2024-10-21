package com.example.adoptr_backend.service.dto.response;

import com.example.adoptr_backend.model.ReportModelType;
import jakarta.persistence.*;
import lombok.Data;

@Data
public class ReportReasonDTO {

    private Long id;

    private String reason;

    private String description;

    private ReportModelType modelType;
}
