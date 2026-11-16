package com.adoptr.adoptr.auth.service;

import com.adoptr.adoptr.auth.service.validator.Validator;
import com.adoptr.adoptr.auth.util.AuthUser;
import com.adoptr.adoptr.profile.service.ProfileService;
import com.adoptr.adoptr.shared.security.JwtUtil;
import com.adoptr.adoptr.user.dto.request.UserDTOin;
import com.adoptr.adoptr.user.model.User;
import com.adoptr.adoptr.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private static final String GOOGLE_URL = "https://lh3.googleusercontent.com/a/foto=s96-c";

    private final Validator validator = mock(Validator.class);
    private final JwtUtil jwtUtil = mock(JwtUtil.class);
    private final UserService userService = mock(UserService.class);
    private final ProfileService profileService = mock(ProfileService.class);
    private final AuthService service = new AuthService(validator, jwtUtil, userService, profileService);

    private final User user = new User();

    @BeforeEach
    void setUp() {
        user.setId(7L);
        when(validator.validate("token")).thenReturn(new AuthUser("sub-1", "a@b.com", "Ana", GOOGLE_URL));
        when(jwtUtil.generateToken(7L)).thenReturn("jwt");
    }

    @Test
    void unUsuarioNuevoSeCreaConSuFotoDeGoogle() {
        when(userService.findByProviderAndProviderUserId("google", "sub-1")).thenReturn(Optional.empty());
        when(userService.createUser(any())).thenReturn(user);

        assertThat(service.authenticate("google", "token").token()).isEqualTo("jwt");

        ArgumentCaptor<UserDTOin> created = ArgumentCaptor.forClass(UserDTOin.class);
        verify(userService).createUser(created.capture());
        assertThat(created.getValue().getGooglePhotoUrl()).isEqualTo(GOOGLE_URL);
        verify(userService, never()).syncGooglePhotoUrl(any(), any());
    }

    @Test
    void unUsuarioExistenteActualizaSuFotoDeGoogleEnCadaLogin() {
        when(userService.findByProviderAndProviderUserId("google", "sub-1")).thenReturn(Optional.of(user));
        when(userService.syncGooglePhotoUrl(user, GOOGLE_URL)).thenReturn(user);

        assertThat(service.authenticate("google", "token").token()).isEqualTo("jwt");

        verify(userService).syncGooglePhotoUrl(user, GOOGLE_URL);
        verify(userService, never()).createUser(any());
    }
}
