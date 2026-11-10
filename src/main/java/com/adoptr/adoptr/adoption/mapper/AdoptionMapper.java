package com.adoptr.adoptr.adoption.mapper;

import com.adoptr.adoptr.adoption.dto.response.AdoptionDTO;
import com.adoptr.adoptr.adoption.dto.response.AdoptionSummaryDTO;
import com.adoptr.adoptr.adoption.dto.response.OwnerDTO;
import com.adoptr.adoptr.adoption.model.AdoptionPublication;
import com.adoptr.adoptr.adoption.model.LifeStage;
import com.adoptr.adoptr.location.mapper.LocationMapper;
import com.adoptr.adoptr.location.model.Locality;
import com.adoptr.adoptr.profile.model.OrganizationProfile;
import com.adoptr.adoptr.profile.model.PersonProfile;
import com.adoptr.adoptr.profile.model.Profile;

import java.time.YearMonth;

/** Mapeo a mano: la etapa de vida depende del mes actual y el responsable sale del perfil del autor. */
public final class AdoptionMapper {

    private AdoptionMapper() {
    }

    public static AdoptionSummaryDTO toSummary(AdoptionPublication publication, YearMonth current) {
        AdoptionSummaryDTO dto = new AdoptionSummaryDTO();
        fill(dto, publication, current);
        return dto;
    }

    public static AdoptionDTO toDto(AdoptionPublication publication, YearMonth current,
                                    OwnerDTO owner, boolean viewerIsOwner) {
        AdoptionDTO dto = new AdoptionDTO();
        fill(dto, publication, current);
        dto.setDescription(publication.getDescription());
        dto.setOwner(owner);
        dto.setViewerIsOwner(viewerIsOwner);
        return dto;
    }

    public static OwnerDTO toOwner(Profile profile) {
        return switch (profile) {
            case null -> null;
            case PersonProfile person ->
                    new OwnerDTO(person.getId(), person.getFirstName() + " " + person.getLastName(),
                            person.getProfileType());
            case OrganizationProfile organization ->
                    new OwnerDTO(organization.getId(), organization.getOrganizationName(),
                            organization.getProfileType());
            default -> throw new IllegalStateException("Tipo de perfil sin mapeo: " + profile.getClass());
        };
    }

    private static void fill(AdoptionSummaryDTO dto, AdoptionPublication p, YearMonth current) {
        YearMonth birth = YearMonth.from(p.getEstimatedBirthDate());
        dto.setId(p.getId());
        dto.setPetName(p.getPetName());
        dto.setSpecies(p.getSpecies());
        dto.setSex(p.getSex());
        dto.setSize(p.getSize());
        dto.setEstimatedBirthDate(birth);
        dto.setLifeStage(LifeStage.of(birth, current));
        dto.setVaccinated(p.isVaccinated());
        dto.setDewormed(p.isDewormed());
        dto.setNeutered(p.isNeutered());
        dto.setStatus(p.getStatus());
        Locality locality = p.getLocality();
        if (locality != null) {
            dto.setLocality(LocationMapper.MAPPER.toDto(locality));
            dto.setProvince(LocationMapper.MAPPER.toDto(locality.getProvince()));
        }
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());
    }
}
