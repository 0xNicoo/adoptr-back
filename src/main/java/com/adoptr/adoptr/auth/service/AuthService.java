package com.adoptr.adoptr.auth.service;

import com.adoptr.adoptr.auth.dto.response.AuthDTO;
import com.adoptr.adoptr.auth.service.validator.Validator;
import com.adoptr.adoptr.auth.util.AuthUser;
import com.adoptr.adoptr.shared.security.JwtUtil;
import com.adoptr.adoptr.user.dto.response.UserDTO;
import com.adoptr.adoptr.user.model.User;
import com.adoptr.adoptr.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final Validator validator;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthService(Validator validator, UserRepository userRepository, JwtUtil jwtUtil) {
        this.validator = validator;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public AuthDTO authenticate(String provider, String token) {
        AuthUser authUser = validator.validate(token);
        //TODO: PASAR ESTA LOGICA A UN USER SERVICE, USAR MAPSTRUCT PARA MAPEAR USER -> USERDTO
        User user = userRepository
                .findByProviderAndProviderUserId(provider, authUser.providerUserId())
                .orElseGet(() -> createNewUser(provider, authUser));
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setProvider(user.getProvider());
        userDTO.setProviderUserId(user.getProviderUserId());
        userDTO.setCreatedAt(user.getCreatedAt());
        String jwt = jwtUtil.generateToken(user.getName(), user.getId());
        return new AuthDTO(jwt, userDTO);
    }

    private User createNewUser(String provider, AuthUser oauthUser) {
        User user = new User();
        user.setProvider(provider);
        user.setProviderUserId(oauthUser.providerUserId());
        user.setEmail(oauthUser.email());
        user.setName(oauthUser.name());
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
}
