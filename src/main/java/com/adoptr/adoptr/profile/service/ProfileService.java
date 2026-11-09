package com.adoptr.adoptr.profile.service;

import com.adoptr.adoptr.profile.dto.request.ProfileDTOin;
import com.adoptr.adoptr.profile.dto.response.ProfileDTO;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {
    ProfileDTO get();

    ProfileDTO create(ProfileDTOin request);

    ProfileDTO setPhoto(MultipartFile file);

    void deletePhoto();

    /** URL de la foto de un perfil, o {@code null} si no tiene. */
    String photoUrl(Long profileId);
}
