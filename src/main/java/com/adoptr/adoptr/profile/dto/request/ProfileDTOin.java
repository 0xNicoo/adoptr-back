package com.adoptr.adoptr.profile.dto.request;

import com.adoptr.adoptr.profile.model.Gender;
import lombok.Data;

@Data
public class ProfileDTOin {
    private String firstName;
    private String lastName;
    private Gender genderType;
    private String description;
    private Long localityId;
}
