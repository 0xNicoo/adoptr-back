package com.adoptr.adoptr.profile.mapper;

import com.adoptr.adoptr.location.mapper.LocationMapper;
import com.adoptr.adoptr.location.model.Locality;
import com.adoptr.adoptr.profile.dto.response.ProfileDTO;
import com.adoptr.adoptr.profile.dto.response.PublicProfileDTO;
import com.adoptr.adoptr.profile.model.OrganizationProfile;
import com.adoptr.adoptr.profile.model.PersonProfile;
import com.adoptr.adoptr.profile.model.Profile;

/**
 * Mapeo escrito a mano: {@link Profile} es abstracta y cada subtipo aporta sus
 * propios campos, algo que MapStruct no resuelve sin configuración extra.
 */
public final class ProfileMapper {

    private ProfileMapper() {
    }

    public static ProfileDTO toDto(Profile profile) {
        if (profile == null) {
            return null;
        }
        ProfileDTO dto = new ProfileDTO();
        dto.setId(profile.getId());
        dto.setProfileType(profile.getProfileType());
        dto.setDescription(profile.getDescription());
        Locality locality = profile.getLocality();
        if (locality != null) {
            dto.setLocality(LocationMapper.MAPPER.toDto(locality));
            dto.setProvince(LocationMapper.MAPPER.toDto(locality.getProvince()));
        }
        switch (profile) {
            case PersonProfile person -> {
                dto.setFirstName(person.getFirstName());
                dto.setLastName(person.getLastName());
                dto.setGenderType(person.getGenderType());
            }
            case OrganizationProfile organization -> {
                dto.setOrganizationName(organization.getOrganizationName());
                dto.setInstagram(organization.getInstagram());
                dto.setFacebook(organization.getFacebook());
                dto.setWebsite(organization.getWebsite());
            }
            default -> throw new IllegalStateException("Tipo de perfil sin mapeo: " + profile.getClass());
        }
        return dto;
    }

    /** Como {@link #toDto} sin el género de la persona. */
    public static PublicProfileDTO toPublicDto(Profile profile) {
        if (profile == null) {
            return null;
        }
        PublicProfileDTO dto = new PublicProfileDTO();
        dto.setId(profile.getId());
        dto.setProfileType(profile.getProfileType());
        dto.setDescription(profile.getDescription());
        Locality locality = profile.getLocality();
        if (locality != null) {
            dto.setLocality(LocationMapper.MAPPER.toDto(locality));
            dto.setProvince(LocationMapper.MAPPER.toDto(locality.getProvince()));
        }
        switch (profile) {
            case PersonProfile person -> {
                dto.setFirstName(person.getFirstName());
                dto.setLastName(person.getLastName());
            }
            case OrganizationProfile organization -> {
                dto.setOrganizationName(organization.getOrganizationName());
                dto.setInstagram(organization.getInstagram());
                dto.setFacebook(organization.getFacebook());
                dto.setWebsite(organization.getWebsite());
            }
            default -> throw new IllegalStateException("Tipo de perfil sin mapeo: " + profile.getClass());
        }
        return dto;
    }
}
