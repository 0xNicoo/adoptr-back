package com.adoptr.adoptr.user.controller;

import com.adoptr.adoptr.user.dto.response.GooglePhotoDTO;
import com.adoptr.adoptr.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/google-photo")
    public GooglePhotoDTO googlePhoto() {
        return userService.getGooglePhoto();
    }
}
