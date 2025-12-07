package com.adoptr.adoptr.profile.dto.response;

import com.adoptr.adoptr.location.model.Locality;
import com.adoptr.adoptr.profile.model.Gender;
import com.adoptr.adoptr.user.dto.response.UserDTO;
import lombok.Data;

@Data
public class ProfileDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private Gender genderType;
    private String description;
    private Locality locality;
}
