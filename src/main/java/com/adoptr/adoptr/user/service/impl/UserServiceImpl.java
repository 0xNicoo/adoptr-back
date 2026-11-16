package com.adoptr.adoptr.user.service.impl;

import com.adoptr.adoptr.shared.util.MembershipSupport;
import com.adoptr.adoptr.user.dto.request.UserDTOin;
import com.adoptr.adoptr.user.dto.response.GooglePhotoDTO;
import com.adoptr.adoptr.user.google.GooglePhotoClient;
import com.adoptr.adoptr.user.mapper.UserMapper;
import com.adoptr.adoptr.user.model.User;
import com.adoptr.adoptr.user.repository.UserRepository;
import com.adoptr.adoptr.user.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final MembershipSupport membershipSupport;

    public UserServiceImpl(UserRepository userRepository, MembershipSupport membershipSupport) {
        this.userRepository = userRepository;
        this.membershipSupport = membershipSupport;
    }

    @Override
    public Optional<User> findByProviderAndProviderUserId(String provider, String providerUserId) {
        return userRepository.findByProviderAndProviderUserId(provider, providerUserId);
    }

    @Override
    public User createUser(UserDTOin userDTOin) {
        User user = UserMapper.MAPPER.toEntity(userDTOin);
        return userRepository.save(user);
    }

    @Override
    public User syncGooglePhotoUrl(User user, String googlePhotoUrl) {
        if (Objects.equals(user.getGooglePhotoUrl(), googlePhotoUrl)) {
            return user;
        }
        user.setGooglePhotoUrl(googlePhotoUrl);
        return userRepository.save(user);
    }

    @Override
    public GooglePhotoDTO getGooglePhoto() {
        String url = userRepository.findById(membershipSupport.getUserId())
                .map(User::getGooglePhotoUrl)
                .filter(GooglePhotoClient::isAllowed)
                .orElse(null);
        return new GooglePhotoDTO(url);
    }
}
