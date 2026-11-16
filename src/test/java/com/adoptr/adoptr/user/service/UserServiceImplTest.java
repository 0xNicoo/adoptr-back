package com.adoptr.adoptr.user.service;

import com.adoptr.adoptr.shared.util.MembershipSupport;
import com.adoptr.adoptr.user.dto.request.UserDTOin;
import com.adoptr.adoptr.user.model.User;
import com.adoptr.adoptr.user.repository.UserRepository;
import com.adoptr.adoptr.user.service.impl.UserServiceImpl;
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

class UserServiceImplTest {

    private static final long USER_ID = 7L;
    private static final String GOOGLE_URL = "https://lh3.googleusercontent.com/a/foto=s96-c";

    private final UserRepository userRepository = mock(UserRepository.class);
    private final MembershipSupport membershipSupport = mock(MembershipSupport.class);
    private final UserService service = new UserServiceImpl(userRepository, membershipSupport);

    private final User user = new User();

    @BeforeEach
    void setUp() {
        user.setId(USER_ID);
        when(membershipSupport.getUserId()).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void alCrearElUsuarioGuardaSuFotoDeGoogle() {
        service.createUser(new UserDTOin("google", "sub-1", "a@b.com", "Ana", GOOGLE_URL));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getGooglePhotoUrl()).isEqualTo(GOOGLE_URL);
    }

    @Test
    void siCambioLaFotoDeGoogleLaActualiza() {
        user.setGooglePhotoUrl("https://lh3.googleusercontent.com/a/vieja");

        User result = service.syncGooglePhotoUrl(user, GOOGLE_URL);

        assertThat(result.getGooglePhotoUrl()).isEqualTo(GOOGLE_URL);
        verify(userRepository).save(user);
    }

    @Test
    void siLaCuentaYaNoTieneFotoLaBorra() {
        user.setGooglePhotoUrl(GOOGLE_URL);

        service.syncGooglePhotoUrl(user, null);

        assertThat(user.getGooglePhotoUrl()).isNull();
        verify(userRepository).save(user);
    }

    @Test
    void siLaFotoNoCambioNoGuardaNada() {
        user.setGooglePhotoUrl(GOOGLE_URL);

        service.syncGooglePhotoUrl(user, GOOGLE_URL);

        verify(userRepository, never()).save(any());
    }

    @Test
    void devuelveLaFotoDeGoogleDelUsuarioDelToken() {
        user.setGooglePhotoUrl(GOOGLE_URL);

        assertThat(service.getGooglePhoto().url()).isEqualTo(GOOGLE_URL);
    }

    @Test
    void sinFotoDeGoogleDevuelveNull() {
        assertThat(service.getGooglePhoto().url()).isNull();
    }

    @Test
    void unaUrlGuardadaQueNoEsDeGoogleNoSeDevuelve() {
        user.setGooglePhotoUrl("https://evil.com/foto.jpg");

        assertThat(service.getGooglePhoto().url()).isNull();
    }
}
