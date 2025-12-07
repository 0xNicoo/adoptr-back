package com.adoptr.adoptr.user.service.impl;

import com.adoptr.adoptr.user.dto.request.UserDTOin;
import com.adoptr.adoptr.user.mapper.UserMapper;
import com.adoptr.adoptr.user.model.User;
import com.adoptr.adoptr.user.repository.UserRepository;
import com.adoptr.adoptr.user.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
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

}
