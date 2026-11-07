package com.adoptr.adoptr.profile.dto.request;

import com.adoptr.adoptr.profile.model.Gender;
import com.adoptr.adoptr.profile.model.ProfileType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileDTOin {
    @NotNull
    private ProfileType profileType;

    @NotBlank
    @Size(max = 500)
    private String description;

    @NotNull
    private Long localityId;

    // Persona
    @Size(max = 100)
    private String firstName;
    @Size(max = 100)
    private String lastName;
    private Gender genderType;

    // Organización
    @Size(max = 150)
    private String organizationName;
    @Size(max = 100)
    private String instagram;
    @Size(max = 100)
    private String facebook;
    @Size(max = 255)
    private String website;
}
