package com.adoptr.adoptr.auth.service;

import com.adoptr.adoptr.auth.dto.response.AuthDTO;
import com.adoptr.adoptr.auth.service.validator.Validator;
import com.adoptr.adoptr.auth.util.AuthUser;
import com.adoptr.adoptr.profile.service.ProfileService;
import com.adoptr.adoptr.shared.security.JwtUtil;
import com.adoptr.adoptr.user.dto.request.UserDTOin;
import com.adoptr.adoptr.user.dto.response.UserDTO;
import com.adoptr.adoptr.user.mapper.UserMapper;
import com.adoptr.adoptr.user.model.User;
import com.adoptr.adoptr.user.repository.UserRepository;
import com.adoptr.adoptr.user.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    private final Validator validator;
    private final JwtUtil jwtUtil;

    private final UserService userService;

    private final ProfileService profileService;

    public AuthService(Validator validator,
                       JwtUtil jwtUtil,
                       UserService userService,
                       ProfileService profileService) {
        this.validator = validator;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.profileService = profileService;
    }

    public AuthDTO authenticate(String provider, String token) {
        AuthUser authUser = validator.validate(token);
        Optional<User> userOptional = userService.findByProviderAndProviderUserId(provider, authUser.providerUserId());
        UserDTOin userDTOin = new UserDTOin(provider, authUser.providerUserId(), authUser.email(), authUser.name());
        User user = userOptional.orElseGet(() -> userService.createUser(userDTOin));
        UserDTO userDTO = UserMapper.MAPPER.toDto(user);
        if (userDTO.getProfile() != null) {
            userDTO.getProfile().setPhotoUrl(profileService.photoUrl(userDTO.getProfile().getId()));
        }
        String jwt = jwtUtil.generateToken(user.getId());
        return new AuthDTO(jwt, userDTO);
    }

}
