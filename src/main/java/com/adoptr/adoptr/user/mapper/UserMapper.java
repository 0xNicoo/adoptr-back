package com.adoptr.adoptr.user.mapper;

import com.adoptr.adoptr.profile.dto.response.ProfileDTO;
import com.adoptr.adoptr.profile.mapper.ProfileMapper;
import com.adoptr.adoptr.profile.model.Profile;
import com.adoptr.adoptr.shared.mapper.EntityMapper;
import com.adoptr.adoptr.user.dto.request.UserDTOin;
import com.adoptr.adoptr.user.dto.response.UserDTO;
import com.adoptr.adoptr.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper extends EntityMapper<UserDTO, UserDTOin, User> {
    UserMapper MAPPER = Mappers.getMapper(UserMapper.class);

    default ProfileDTO profileToDto(Profile profile) {
        return ProfileMapper.toDto(profile);
    }

    @Mapping(target = "profile", ignore = true)
    User fromDto(UserDTO dto);
}
