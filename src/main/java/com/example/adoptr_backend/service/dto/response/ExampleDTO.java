package com.example.adoptr_backend.service.dto.response;

import com.example.adoptr_backend.model.ExampleType;
import lombok.Data;

@Data
public class ExampleDTO {
    private Long id;
    private String title;
    private String text;
    private ExampleType type;
    private Boolean active;
    private Long createdByUser;
}
