package com.adoptr.adoptr.user.service;

import com.adoptr.adoptr.user.dto.request.UserDTOin;
import com.adoptr.adoptr.user.model.User;

import java.util.Optional;

public interface UserService {
    Optional<User> findByProviderAndProviderUserId(String provider, String providerUserId);
    User createUser(UserDTOin userDTOin);
}
