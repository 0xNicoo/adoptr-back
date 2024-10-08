package com.example.adoptr_backend.service.dto.response;

import com.example.adoptr_backend.model.ExampleType;
import com.example.adoptr_backend.model.GenderType;
import com.example.adoptr_backend.model.User;
import lombok.Data;

@Data
public class ProfileDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String description;
    private GenderType genderType;
    private String s3Url;
    private UserDTO user;
    private LocalityDTO locality;
}
