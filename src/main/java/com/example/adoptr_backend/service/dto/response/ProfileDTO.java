package com.example.adoptr_backend.service.dto.response;

import com.example.adoptr_backend.model.ExampleType;
import com.example.adoptr_backend.model.User;
import lombok.Data;

@Data
public class ProfileDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String description;
    private Boolean gender;
    private UserDTO user;
}
