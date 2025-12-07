package com.adoptr.adoptr.profile.service.impl;

import com.adoptr.adoptr.profile.dto.response.ProfileDTO;
import com.adoptr.adoptr.profile.error.ProfileError;
import com.adoptr.adoptr.profile.mapper.ProfileMapper;
import com.adoptr.adoptr.profile.model.Profile;
import com.adoptr.adoptr.profile.repository.ProfileRepository;
import com.adoptr.adoptr.profile.service.ProfileService;
import com.adoptr.adoptr.shared.exception.custom.BadRequestException;
import com.adoptr.adoptr.shared.util.MembershipSupport;
import org.springframework.stereotype.Service;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final MembershipSupport membershipSupport;
    private final ProfileRepository profileRepository;

    public ProfileServiceImpl(MembershipSupport membershipSupport,
                              ProfileRepository profileRepository) {
        this.membershipSupport = membershipSupport;
        this.profileRepository = profileRepository;
    }

    @Override
    public ProfileDTO get() {
        Long userId = membershipSupport.getUserId();
        Profile profile = getById(userId);
        return ProfileMapper.MAPPER.toDto(profile);
    }

    private Profile getById(Long id){
        return profileRepository.findByUserId(id).orElseThrow(() -> new BadRequestException(ProfileError.NOT_FOUND));
    }
}
