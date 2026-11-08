package com.adoptr.adoptr.adoption.dto.response;

import com.adoptr.adoptr.profile.model.ProfileType;

/** Responsable visible públicamente: solo nombre para mostrar y tipo de perfil, nunca email ni ids. */
public record OwnerDTO(String displayName, ProfileType profileType) {
}
