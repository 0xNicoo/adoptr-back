package com.adoptr.adoptr.profile.mapper;

import com.adoptr.adoptr.profile.dto.request.ProfileDTOin;
import com.adoptr.adoptr.profile.dto.response.ProfileDTO;
import com.adoptr.adoptr.profile.model.Profile;
import com.adoptr.adoptr.shared.mapper.EntityMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProfileMapper extends EntityMapper<ProfileDTO, ProfileDTOin, Profile> {
    ProfileMapper MAPPER = Mappers.getMapper(ProfileMapper.class);
}
