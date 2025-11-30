package com.adoptr.adoptr.auth.dto.response;

import com.adoptr.adoptr.user.dto.response.UserDTO;

public record AuthDTO(String token, UserDTO user){}
