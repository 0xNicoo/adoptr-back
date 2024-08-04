package com.example.adoptr_backend.service.dto.request;

import com.example.adoptr_backend.model.ExampleType;
import lombok.Data;

@Data
public class ExampleDTOin {
    private String title;
    private String text;
    private ExampleType type;
    private Boolean active;
}
