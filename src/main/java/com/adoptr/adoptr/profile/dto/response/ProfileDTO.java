package com.adoptr.adoptr.profile.dto.response;

import com.adoptr.adoptr.location.dto.response.LocalityDTO;
import com.adoptr.adoptr.location.dto.response.ProvinceDTO;
import com.adoptr.adoptr.profile.model.Gender;
import com.adoptr.adoptr.profile.model.ProfileType;
import lombok.Data;

@Data
public class ProfileDTO {
    private Long id;
    private ProfileType profileType;
    private String description;
    /** {@code null} si no tiene foto. */
    private String photoUrl;
    private LocalityDTO locality;
    private ProvinceDTO province;

    // Persona
    private String firstName;
    private String lastName;
    private Gender genderType;

    // Organización
    private String organizationName;
    private String instagram;
    private String facebook;
    private String website;
}
