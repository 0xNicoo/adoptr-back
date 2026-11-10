package com.adoptr.adoptr.adoption.dto.response;

import com.adoptr.adoptr.profile.model.ProfileType;

/**
 * Responsable visible públicamente: id de su perfil (para enlazarlo), nombre para mostrar y tipo de perfil.
 * Nunca email ni ids del usuario.
 */
public record OwnerDTO(Long profileId, String displayName, ProfileType profileType) {
}
