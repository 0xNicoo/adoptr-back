package com.adoptr.adoptr.profile.service;

import com.adoptr.adoptr.profile.dto.request.ProfileDTOin;
import com.adoptr.adoptr.profile.dto.request.ProfileUpdateDTOin;
import com.adoptr.adoptr.profile.dto.response.ProfileDTO;
import com.adoptr.adoptr.profile.dto.response.PublicProfileDTO;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {
    ProfileDTO get();

    /** Perfil de cualquier usuario, para mostrarlo públicamente. */
    PublicProfileDTO getPublic(Long id);

    ProfileDTO create(ProfileDTOin request);

    ProfileDTO update(ProfileUpdateDTOin request);

    ProfileDTO setPhoto(MultipartFile file);

    ProfileDTO setGooglePhoto();

    void deletePhoto();

    /** URL de la foto de un perfil, o {@code null} si no tiene. */
    String photoUrl(Long profileId);
}
