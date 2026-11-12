package com.adoptr.adoptr.favorite.service;

import com.adoptr.adoptr.adoption.dto.response.AdoptionSummaryDTO;
import com.adoptr.adoptr.adoption.model.AdoptionPublication;
import com.adoptr.adoptr.adoption.model.Species;
import com.adoptr.adoptr.adoption.repository.AdoptionPublicationRepository;
import com.adoptr.adoptr.adoption.service.impl.AdoptionSummaries;
import com.adoptr.adoptr.favorite.model.Favorite;
import com.adoptr.adoptr.favorite.repository.FavoriteRepository;
import com.adoptr.adoptr.favorite.service.impl.FavoriteServiceImpl;
import com.adoptr.adoptr.shared.config.ClockConfig;
import com.adoptr.adoptr.shared.dto.PageDTO;
import com.adoptr.adoptr.shared.exception.custom.ForbiddenException;
import com.adoptr.adoptr.shared.exception.custom.NotFoundException;
import com.adoptr.adoptr.shared.exception.custom.UnauthorizedException;
import com.adoptr.adoptr.shared.exception.error.CommonError;
import com.adoptr.adoptr.shared.util.MembershipSupport;
import com.adoptr.adoptr.user.model.User;
import com.adoptr.adoptr.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FavoriteServiceImplTest {

    private static final long USER_ID = 7L;
    private static final long AUTHOR_ID = 8L;
    private static final long PUBLICATION_ID = 1L;
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-07T15:00:00Z"), ClockConfig.ZONE);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 7, 12, 0);

    private MembershipSupport membershipSupport;
    private FavoriteRepository favoriteRepository;
    private AdoptionPublicationRepository adoptionRepository;
    private UserRepository userRepository;
    private AdoptionSummaries summaries;
    private FavoriteService service;

    private User viewer;
    private AdoptionPublication publication;

    @BeforeEach
    void setUp() {
        membershipSupport = mock(MembershipSupport.class);
        favoriteRepository = mock(FavoriteRepository.class);
        adoptionRepository = mock(AdoptionPublicationRepository.class);
        userRepository = mock(UserRepository.class);
        summaries = mock(AdoptionSummaries.class);
        service = new FavoriteServiceImpl(membershipSupport, favoriteRepository, adoptionRepository, userRepository,
                summaries, CLOCK);

        viewer = user(USER_ID);
        publication = new AdoptionPublication();
        publication.setId(PUBLICATION_ID);
        publication.setAuthor(user(AUTHOR_ID));

        when(membershipSupport.getUserId()).thenReturn(USER_ID);
        when(userRepository.getReferenceById(USER_ID)).thenReturn(viewer);
        when(adoptionRepository.findByIdAndDeletedAtIsNull(PUBLICATION_ID)).thenReturn(Optional.of(publication));
    }

    private static User user(long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    @Test
    void guardaConLaFechaDelReloj() {
        service.save(PUBLICATION_ID);

        ArgumentCaptor<Favorite> captor = ArgumentCaptor.forClass(Favorite.class);
        verify(favoriteRepository).saveAndFlush(captor.capture());
        Favorite saved = captor.getValue();
        assertThat(saved.getUser()).isSameAs(viewer);
        assertThat(saved.getPublication()).isSameAs(publication);
        assertThat(saved.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void guardarDosVecesNoDuplica() {
        when(favoriteRepository.existsByUserIdAndPublicationId(USER_ID, PUBLICATION_ID)).thenReturn(true);

        service.save(PUBLICATION_ID);

        verify(favoriteRepository, never()).saveAndFlush(any());
    }

    @Test
    void guardarEnSimultaneoNoFalla() {
        when(favoriteRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicada"));

        assertThatCode(() -> service.save(PUBLICATION_ID)).doesNotThrowAnyException();
    }

    @Test
    void noSePuedeGuardarUnaPublicacionPropia() {
        publication.setAuthor(viewer);

        assertThatThrownBy(() -> service.save(PUBLICATION_ID))
                .isInstanceOf(ForbiddenException.class)
                .hasFieldOrPropertyWithValue("code", "FAVORITE_OWN_PUBLICATION");
        verify(favoriteRepository, never()).saveAndFlush(any());
    }

    @Test
    void guardarUnaInexistenteOBorradaEs404() {
        when(adoptionRepository.findByIdAndDeletedAtIsNull(PUBLICATION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.save(PUBLICATION_ID))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", "PUBLICATION_NOT_FOUND");
        verify(favoriteRepository, never()).saveAndFlush(any());
    }

    @Test
    void quitarBorraLaDelUsuarioSinConsultarLaPublicacion() {
        service.unsave(PUBLICATION_ID);

        verify(favoriteRepository).deleteByUserIdAndPublicationId(USER_ID, PUBLICATION_ID);
        verifyNoInteractions(adoptionRepository);
    }

    @Test
    void listaConEspecieYPagina() {
        Page<AdoptionPublication> page = new PageImpl<>(List.of(publication));
        PageDTO<AdoptionSummaryDTO> expected = new PageDTO<>(List.of(new AdoptionSummaryDTO()), 1, 12, 13, 2);
        when(favoriteRepository.findAdoptions(eq(USER_ID), eq(Species.DOG), any())).thenReturn(page);
        when(summaries.page(page)).thenReturn(expected);

        PageDTO<AdoptionSummaryDTO> result = service.list(Species.DOG, 1, 12);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(favoriteRepository).findAdoptions(eq(USER_ID), eq(Species.DOG), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(captor.getValue().getPageSize()).isEqualTo(12);
        assertThat(captor.getValue().getSort().isUnsorted()).isTrue();
        assertThat(result).isSameAs(expected);
    }

    @Test
    void idsDelUsuarioDelToken() {
        when(favoriteRepository.findPublicationIds(USER_ID)).thenReturn(List.of(3L, 1L));

        assertThat(service.ids().publicationIds()).containsExactly(3L, 1L);
    }

    @Test
    void todoRequiereUsuario() {
        when(membershipSupport.getUserId()).thenThrow(new UnauthorizedException(CommonError.UNAUTHENTICATED));

        assertThatThrownBy(() -> service.save(PUBLICATION_ID)).isInstanceOf(UnauthorizedException.class);
        assertThatThrownBy(() -> service.unsave(PUBLICATION_ID)).isInstanceOf(UnauthorizedException.class);
        assertThatThrownBy(() -> service.list(null, 0, 12)).isInstanceOf(UnauthorizedException.class);
        assertThatThrownBy(() -> service.ids()).isInstanceOf(UnauthorizedException.class);
        verifyNoInteractions(favoriteRepository);
    }
}
