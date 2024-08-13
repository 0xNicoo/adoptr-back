package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.model.User;
import com.example.adoptr_backend.repository.UserRepository;
import com.example.adoptr_backend.service.dto.request.UserDTOin;
import com.example.adoptr_backend.service.dto.response.UserDTO;
import com.example.adoptr_backend.service.mapper.UserMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;



@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    //TODO: Ver como usar mapstruct para mapear el user/userdto
    public void register(UserDTOin dto) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        userRepository.save(user);
    }

    public UserDTO authenticate(UserDTOin dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.getEmail(),
                        dto.getPassword()
                )
        );
        User user = userRepository.findByEmail(dto.getEmail()).orElseThrow();
        return new UserDTO(user.getEmail());
    }
}