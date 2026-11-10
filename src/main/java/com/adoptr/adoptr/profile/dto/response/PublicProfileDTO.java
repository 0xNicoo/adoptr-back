package com.adoptr.adoptr.profile.dto.response;

import com.adoptr.adoptr.location.dto.response.LocalityDTO;
import com.adoptr.adoptr.location.dto.response.ProvinceDTO;
import com.adoptr.adoptr.profile.model.ProfileType;
import lombok.Data;

/**
 * Perfil visible para cualquiera ({@code GET /profiles/{id}}). Igual que {@link ProfileDTO} sin el género:
 * la interfaz no lo muestra y es un dato personal que no hace falta exponer. Nunca incluye email ni ids del usuario.
 */
@Data
public class PublicProfileDTO {
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

    // Organización
    private String organizationName;
    private String instagram;
    private String facebook;
    private String website;
}
