package com.adoptr.adoptr.adoption.dto.response;

import com.adoptr.adoptr.profile.model.ProfileType;

/**
 * Responsable visible públicamente: id de su perfil (para enlazarlo), nombre para mostrar, tipo de perfil
 * y URL de su foto ({@code null} si no tiene).
 * Nunca email ni ids del usuario.
 */
public record OwnerDTO(Long profileId, String displayName, ProfileType profileType, String photoUrl) {
}
