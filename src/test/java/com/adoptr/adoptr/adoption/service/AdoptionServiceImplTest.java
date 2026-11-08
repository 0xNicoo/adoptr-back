package com.adoptr.adoptr.adoption.service;

import com.adoptr.adoptr.adoption.dto.request.AdoptionDTOin;
import com.adoptr.adoptr.adoption.dto.request.AdoptionFilterDTO;
import com.adoptr.adoptr.adoption.dto.request.AdoptionStatusDTOin;
import com.adoptr.adoptr.adoption.dto.response.AdoptionDTO;
import com.adoptr.adoptr.adoption.dto.response.AdoptionSummaryDTO;
import com.adoptr.adoptr.adoption.model.AdoptionPublication;
import com.adoptr.adoptr.adoption.model.AdoptionStatus;
import com.adoptr.adoptr.adoption.model.LifeStage;
import com.adoptr.adoptr.adoption.model.PetSex;
import com.adoptr.adoptr.adoption.model.PetSize;
import com.adoptr.adoptr.adoption.model.Species;
import com.adoptr.adoptr.adoption.repository.AdoptionPublicationRepository;
import com.adoptr.adoptr.adoption.service.impl.AdoptionServiceImpl;
import com.adoptr.adoptr.location.model.Locality;
import com.adoptr.adoptr.location.model.Province;
import com.adoptr.adoptr.location.repository.LocalityRepository;
import com.adoptr.adoptr.profile.model.OrganizationProfile;
import com.adoptr.adoptr.profile.model.PersonProfile;
import com.adoptr.adoptr.profile.model.ProfileType;
import com.adoptr.adoptr.profile.repository.ProfileRepository;
import com.adoptr.adoptr.publication.model.PublicationType;
import com.adoptr.adoptr.shared.config.ClockConfig;
import com.adoptr.adoptr.shared.dto.PageDTO;
import com.adoptr.adoptr.shared.exception.custom.BadRequestException;
import com.adoptr.adoptr.shared.exception.custom.ForbiddenException;
import com.adoptr.adoptr.shared.exception.custom.NotFoundException;
import com.adoptr.adoptr.shared.util.MembershipSupport;
import com.adoptr.adoptr.user.model.User;
import com.adoptr.adoptr.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdoptionServiceImplTest {

    private static final long USER_ID = 7L;
    private static final long OTHER_ID = 8L;
    private static final long LOCALITY_ID = 6441030L;
    /** 2026-11-07 12:00 en Argentina. */
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-07T15:00:00Z"), ClockConfig.ZONE);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 7, 12, 0);
    private static final LocalDateTime CREATED = LocalDateTime.of(2026, 10, 1, 9, 30);

    private MembershipSupport membershipSupport;
    private AdoptionPublicationRepository adoptionRepository;
    private ProfileRepository profileRepository;
    private LocalityRepository localityRepository;
    private UserRepository userRepository;
    private AdoptionService service;

    private Locality locality;
    private User owner;
    private User other;

    @BeforeEach
    void setUp() {
        membershipSupport = mock(MembershipSupport.class);
        adoptionRepository = mock(AdoptionPublicationRepository.class);
        profileRepository = mock(ProfileRepository.class);
        localityRepository = mock(LocalityRepository.class);
        userRepository = mock(UserRepository.class);
        service = new AdoptionServiceImpl(membershipSupport, adoptionRepository, profileRepository,
                localityRepository, userRepository, CLOCK);

        Province province = new Province();
        province.setId(6L);
        province.setName("Buenos Aires");
        locality = new Locality();
        locality.setId(LOCALITY_ID);
        locality.setName("La Plata");
        locality.setDisplayName("La Plata");
        locality.setProvince(province);
        owner = user(USER_ID);
        other = user(OTHER_ID);

        when(localityRepository.findById(LOCALITY_ID)).thenReturn(Optional.of(locality));
        when(userRepository.getReferenceById(USER_ID)).thenReturn(owner);
        when(adoptionRepository.save(any(AdoptionPublication.class))).thenAnswer(invocation -> {
            AdoptionPublication p = invocation.getArgument(0);
            if (p.getId() == null) {
                p.setId(1L);
            }
            return p;
        });
    }

    private static User user(long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private void loggedAs(long userId) {
        when(membershipSupport.getUserId()).thenReturn(userId);
        when(membershipSupport.findUserId()).thenReturn(Optional.of(userId));
    }

    private void anonymous() {
        when(membershipSupport.findUserId()).thenReturn(Optional.empty());
    }

    private static AdoptionDTOin dogInput() {
        AdoptionDTOin in = new AdoptionDTOin();
        in.setPetName("  Luna  ");
        in.setDescription("  Muy cariñosa.  ");
        in.setSpecies(Species.DOG);
        in.setSex(PetSex.FEMALE);
        in.setSize(PetSize.MEDIUM);
        in.setEstimatedBirthDate(YearMonth.of(2024, 3));
        in.setVaccinated(true);
        in.setDewormed(true);
        in.setNeutered(false);
        in.setLocalityId(LOCALITY_ID);
        return in;
    }

    private static AdoptionDTOin catInput() {
        AdoptionDTOin in = dogInput();
        in.setPetName("Michi");
        in.setSpecies(Species.CAT);
        in.setSize(null);
        return in;
    }

    private AdoptionPublication existing(User author, AdoptionStatus status) {
        AdoptionPublication p = new AdoptionPublication();
        p.setId(1L);
        p.setType(PublicationType.ADOPTION);
        p.setAuthor(author);
        p.setLocality(locality);
        p.setDescription("Original.");
        p.setCreatedAt(CREATED);
        p.setUpdatedAt(CREATED);
        p.setPetName("Original");
        p.setSpecies(Species.DOG);
        p.setSex(PetSex.MALE);
        p.setSize(PetSize.LARGE);
        p.setEstimatedBirthDate(LocalDate.of(2020, 1, 1));
        p.setVaccinated(false);
        p.setDewormed(false);
        p.setNeutered(true);
        p.setStatus(status);
        when(adoptionRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(p));
        return p;
    }

    private void deleted() {
        when(adoptionRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());
    }

    private static PersonProfile person() {
        PersonProfile profile = new PersonProfile();
        profile.setFirstName("Dolores");
        profile.setLastName("Dominguez");
        return profile;
    }

    private AdoptionPublication savedPublication() {
        ArgumentCaptor<AdoptionPublication> captor = ArgumentCaptor.forClass(AdoptionPublication.class);
        verify(adoptionRepository).save(captor.capture());
        return captor.getValue();
    }

    // ---- Alta ----

    @Test
    void crearAsignaDefaultsYAutorDelToken() {
        loggedAs(USER_ID);
        when(profileRepository.existsByUserId(USER_ID)).thenReturn(true);
        when(profileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(person()));

        AdoptionDTO dto = service.create(dogInput());

        AdoptionPublication saved = savedPublication();
        assertThat(saved.getType()).isEqualTo(PublicationType.ADOPTION);
        assertThat(saved.getStatus()).isEqualTo(AdoptionStatus.AVAILABLE);
        assertThat(saved.getCreatedAt()).isEqualTo(NOW);
        assertThat(saved.getUpdatedAt()).isEqualTo(NOW);
        assertThat(saved.getDeletedAt()).isNull();
        assertThat(saved.getAuthor()).isSameAs(owner);
        assertThat(saved.getLocality()).isSameAs(locality);
        assertThat(saved.getEstimatedBirthDate()).isEqualTo(LocalDate.of(2024, 3, 1));
        assertThat(saved.getPetName()).isEqualTo("Luna");
        assertThat(saved.getDescription()).isEqualTo("Muy cariñosa.");
        assertThat(saved.getSize()).isEqualTo(PetSize.MEDIUM);
        assertThat(saved.isVaccinated()).isTrue();
        assertThat(saved.isDewormed()).isTrue();
        assertThat(saved.isNeutered()).isFalse();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getEstimatedBirthDate()).isEqualTo(YearMonth.of(2024, 3));
        assertThat(dto.getLifeStage()).isEqualTo(LifeStage.ADULT);
        assertThat(dto.isViewerIsOwner()).isTrue();
        assertThat(dto.getOwner().displayName()).isEqualTo("Dolores Dominguez");
        assertThat(dto.getLocality().getDisplayName()).isEqualTo("La Plata");
        assertThat(dto.getProvince().getName()).isEqualTo("Buenos Aires");
    }

    @Test
    void crearSinPerfilDevuelveProfileRequired() {
        loggedAs(USER_ID);
        when(profileRepository.existsByUserId(USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.create(dogInput()))
                .isInstanceOf(ForbiddenException.class)
                .hasFieldOrPropertyWithValue("code", "PROFILE_REQUIRED");
        verify(adoptionRepository, never()).save(any());
    }

    @Test
    void crearConLocalidadInexistenteFalla() {
        loggedAs(USER_ID);
        when(profileRepository.existsByUserId(USER_ID)).thenReturn(true);
        AdoptionDTOin in = dogInput();
        in.setLocalityId(999L);

        assertThatThrownBy(() -> service.create(in))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", "LOCALITY_NOT_FOUND");
        verify(adoptionRepository, never()).save(any());
    }

    @Test
    void perroSinTamanoFalla() {
        loggedAs(USER_ID);
        when(profileRepository.existsByUserId(USER_ID)).thenReturn(true);
        AdoptionDTOin in = dogInput();
        in.setSize(null);

        assertThatThrownBy(() -> service.create(in))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", "VALIDATION_ERROR")
                .hasMessageContaining("size: obligatorio para perros");
    }

    @Test
    void gatoConTamanoFalla() {
        loggedAs(USER_ID);
        when(profileRepository.existsByUserId(USER_ID)).thenReturn(true);
        AdoptionDTOin in = catInput();
        in.setSize(PetSize.SMALL);

        assertThatThrownBy(() -> service.create(in))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", "VALIDATION_ERROR")
                .hasMessageContaining("size: no corresponde para gatos");
    }

    @Test
    void gatoSinTamanoEsValido() {
        loggedAs(USER_ID);
        when(profileRepository.existsByUserId(USER_ID)).thenReturn(true);

        AdoptionDTO dto = service.create(catInput());

        assertThat(dto.getSize()).isNull();
        assertThat(savedPublication().getSpecies()).isEqualTo(Species.CAT);
    }

    @Test
    void fechaFuturaFalla() {
        loggedAs(USER_ID);
        when(profileRepository.existsByUserId(USER_ID)).thenReturn(true);
        AdoptionDTOin in = dogInput();
        in.setEstimatedBirthDate(YearMonth.of(2026, 12));

        assertThatThrownBy(() -> service.create(in))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("estimatedBirthDate: no puede ser futura");
    }

    @Test
    void fechaDelMesActualEsValida() {
        loggedAs(USER_ID);
        when(profileRepository.existsByUserId(USER_ID)).thenReturn(true);
        AdoptionDTOin in = dogInput();
        in.setEstimatedBirthDate(YearMonth.of(2026, 11));

        AdoptionDTO dto = service.create(in);

        assertThat(dto.getLifeStage()).isEqualTo(LifeStage.PUPPY);
    }

    @Test
    void fechaDeTreintaAniosExactosEsValida() {
        loggedAs(USER_ID);
        when(profileRepository.existsByUserId(USER_ID)).thenReturn(true);
        AdoptionDTOin in = dogInput();
        in.setEstimatedBirthDate(YearMonth.of(1996, 11));

        AdoptionDTO dto = service.create(in);

        assertThat(dto.getLifeStage()).isEqualTo(LifeStage.SENIOR);
    }

    @Test
    void fechaDeMasDeTreintaAniosFalla() {
        loggedAs(USER_ID);
        when(profileRepository.existsByUserId(USER_ID)).thenReturn(true);
        AdoptionDTOin in = dogInput();
        in.setEstimatedBirthDate(YearMonth.of(1996, 10));

        assertThatThrownBy(() -> service.create(in))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("estimatedBirthDate: no puede ser de hace más de 30 años");
    }

    // ---- Edición ----

    @Test
    void editarPorDuenoReemplazaFichaYActualizaUpdatedAt() {
        loggedAs(USER_ID);
        AdoptionPublication p = existing(owner, AdoptionStatus.ADOPTED);

        AdoptionDTO dto = service.update(1L, dogInput());

        assertThat(p.getPetName()).isEqualTo("Luna");
        assertThat(p.getDescription()).isEqualTo("Muy cariñosa.");
        assertThat(p.getSex()).isEqualTo(PetSex.FEMALE);
        assertThat(p.getSize()).isEqualTo(PetSize.MEDIUM);
        assertThat(p.getEstimatedBirthDate()).isEqualTo(LocalDate.of(2024, 3, 1));
        assertThat(p.isVaccinated()).isTrue();
        assertThat(p.isNeutered()).isFalse();
        assertThat(p.getUpdatedAt()).isEqualTo(NOW);
        assertThat(p.getCreatedAt()).isEqualTo(CREATED);
        assertThat(p.getStatus()).isEqualTo(AdoptionStatus.ADOPTED);
        assertThat(p.getAuthor()).isSameAs(owner);
        assertThat(dto.isViewerIsOwner()).isTrue();
        verify(adoptionRepository).save(p);
    }

    @Test
    void editarAGatoConTamanoFalla() {
        loggedAs(USER_ID);
        existing(owner, AdoptionStatus.AVAILABLE);
        AdoptionDTOin in = dogInput();
        in.setSpecies(Species.CAT);

        assertThatThrownBy(() -> service.update(1L, in))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("size: no corresponde para gatos");
    }

    @Test
    void editarAPerroSinTamanoFalla() {
        loggedAs(USER_ID);
        AdoptionPublication p = existing(owner, AdoptionStatus.AVAILABLE);
        p.setSpecies(Species.CAT);
        p.setSize(null);
        AdoptionDTOin in = catInput();
        in.setSpecies(Species.DOG);

        assertThatThrownBy(() -> service.update(1L, in))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("size: obligatorio para perros");
        verify(adoptionRepository, never()).save(any());
    }

    @Test
    void editarAjenaDevuelve403() {
        loggedAs(OTHER_ID);
        existing(owner, AdoptionStatus.AVAILABLE);

        assertThatThrownBy(() -> service.update(1L, dogInput()))
                .isInstanceOf(ForbiddenException.class)
                .hasFieldOrPropertyWithValue("code", "ADOPTION_NOT_OWNER");
        verify(adoptionRepository, never()).save(any());
    }

    @Test
    void editarAjenaConDatosInvalidosDevuelve403() {
        loggedAs(OTHER_ID);
        existing(owner, AdoptionStatus.AVAILABLE);
        AdoptionDTOin in = dogInput();
        in.setSize(null);

        assertThatThrownBy(() -> service.update(1L, in))
                .isInstanceOf(ForbiddenException.class)
                .hasFieldOrPropertyWithValue("code", "ADOPTION_NOT_OWNER");
    }

    @Test
    void editarBorradaDevuelve404() {
        loggedAs(USER_ID);
        deleted();

        assertThatThrownBy(() -> service.update(1L, dogInput()))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", "ADOPTION_NOT_FOUND");
    }

    @Test
    void editarAjenaBorradaDevuelve404() {
        loggedAs(OTHER_ID);
        deleted();

        assertThatThrownBy(() -> service.update(1L, dogInput()))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", "ADOPTION_NOT_FOUND");
    }

    // ---- Estado ----

    private static AdoptionStatusDTOin status(AdoptionStatus status) {
        AdoptionStatusDTOin in = new AdoptionStatusDTOin();
        in.setStatus(status);
        return in;
    }

    @Test
    void marcarAdoptadaActualizaEstadoYUpdatedAt() {
        loggedAs(USER_ID);
        AdoptionPublication p = existing(owner, AdoptionStatus.AVAILABLE);

        AdoptionDTO dto = service.changeStatus(1L, status(AdoptionStatus.ADOPTED));

        assertThat(p.getStatus()).isEqualTo(AdoptionStatus.ADOPTED);
        assertThat(p.getUpdatedAt()).isEqualTo(NOW);
        assertThat(dto.getStatus()).isEqualTo(AdoptionStatus.ADOPTED);
    }

    @Test
    void volverADisponibleFunciona() {
        loggedAs(USER_ID);
        AdoptionPublication p = existing(owner, AdoptionStatus.ADOPTED);

        service.changeStatus(1L, status(AdoptionStatus.AVAILABLE));

        assertThat(p.getStatus()).isEqualTo(AdoptionStatus.AVAILABLE);
    }

    @Test
    void mismoEstadoNoTocaUpdatedAt() {
        loggedAs(USER_ID);
        AdoptionPublication p = existing(owner, AdoptionStatus.ADOPTED);

        AdoptionDTO dto = service.changeStatus(1L, status(AdoptionStatus.ADOPTED));

        assertThat(p.getUpdatedAt()).isEqualTo(CREATED);
        assertThat(dto.getStatus()).isEqualTo(AdoptionStatus.ADOPTED);
        verify(adoptionRepository, never()).save(any());
    }

    @Test
    void cambiarEstadoAjenaDevuelve403() {
        loggedAs(OTHER_ID);
        AdoptionPublication p = existing(owner, AdoptionStatus.AVAILABLE);

        assertThatThrownBy(() -> service.changeStatus(1L, status(AdoptionStatus.ADOPTED)))
                .isInstanceOf(ForbiddenException.class)
                .hasFieldOrPropertyWithValue("code", "ADOPTION_NOT_OWNER");
        assertThat(p.getStatus()).isEqualTo(AdoptionStatus.AVAILABLE);
    }

    // ---- Baja ----

    @Test
    void bajaPoneDeletedAt() {
        loggedAs(USER_ID);
        AdoptionPublication p = existing(owner, AdoptionStatus.AVAILABLE);

        service.delete(1L);

        assertThat(p.getDeletedAt()).isEqualTo(NOW);
        verify(adoptionRepository).save(p);
    }

    @Test
    void bajaAjenaDevuelve403() {
        loggedAs(OTHER_ID);
        AdoptionPublication p = existing(owner, AdoptionStatus.AVAILABLE);

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(ForbiddenException.class)
                .hasFieldOrPropertyWithValue("code", "ADOPTION_NOT_OWNER");
        assertThat(p.getDeletedAt()).isNull();
    }

    @Test
    void bajaRepetidaDevuelve404() {
        loggedAs(USER_ID);
        deleted();

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", "ADOPTION_NOT_FOUND");
    }

    // ---- Detalle ----

    @Test
    void detalleAnonimoNoEsDueno() {
        anonymous();
        existing(owner, AdoptionStatus.AVAILABLE);

        assertThat(service.get(1L).isViewerIsOwner()).isFalse();
    }

    @Test
    void detalleDelDuenoEsDueno() {
        loggedAs(USER_ID);
        existing(owner, AdoptionStatus.AVAILABLE);

        assertThat(service.get(1L).isViewerIsOwner()).isTrue();
    }

    @Test
    void detalleDeOtroNoEsDueno() {
        loggedAs(OTHER_ID);
        existing(owner, AdoptionStatus.AVAILABLE);

        assertThat(service.get(1L).isViewerIsOwner()).isFalse();
    }

    @Test
    void detalleDeAdoptadaSeDevuelve() {
        anonymous();
        existing(owner, AdoptionStatus.ADOPTED);

        assertThat(service.get(1L).getStatus()).isEqualTo(AdoptionStatus.ADOPTED);
    }

    @Test
    void detalleInexistenteDevuelve404() {
        anonymous();
        deleted();

        assertThatThrownBy(() -> service.get(1L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", "ADOPTION_NOT_FOUND");
    }

    @Test
    void detalleArmaOwnerDePersonaYDeOrganizacion() {
        anonymous();
        existing(owner, AdoptionStatus.AVAILABLE);
        when(profileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(person()));

        AdoptionDTO personDto = service.get(1L);

        OrganizationProfile organization = new OrganizationProfile();
        organization.setOrganizationName("Refugio Patitas");
        when(profileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(organization));

        AdoptionDTO organizationDto = service.get(1L);

        assertThat(personDto.getOwner().displayName()).isEqualTo("Dolores Dominguez");
        assertThat(personDto.getOwner().profileType()).isEqualTo(ProfileType.PERSON);
        assertThat(organizationDto.getOwner().displayName()).isEqualTo("Refugio Patitas");
        assertThat(organizationDto.getOwner().profileType()).isEqualTo(ProfileType.ORGANIZATION);
    }

    // ---- Mis publicaciones ----

    @Test
    void mineUsaUsuarioDelTokenYOrdenDescendente() {
        loggedAs(USER_ID);
        AdoptionPublication p = existing(owner, AdoptionStatus.ADOPTED);
        Page<AdoptionPublication> page = new PageImpl<>(List.of(p), org.springframework.data.domain.PageRequest.of(2, 10), 21);
        when(adoptionRepository.findByAuthorIdAndDeletedAtIsNull(eq(USER_ID), any(Pageable.class))).thenReturn(page);

        PageDTO<AdoptionSummaryDTO> result = service.mine(2, 10);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(adoptionRepository).findByAuthorIdAndDeletedAtIsNull(eq(USER_ID), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(2);
        assertThat(captor.getValue().getPageSize()).isEqualTo(10);
        assertThat(captor.getValue().getSort())
                .isEqualTo(Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().getStatus()).isEqualTo(AdoptionStatus.ADOPTED);
        assertThat(result.items().getFirst().getLifeStage()).isEqualTo(LifeStage.ADULT);
        assertThat(result.totalItems()).isEqualTo(21);
        assertThat(result.totalPages()).isEqualTo(3);
    }

    @Test
    void mineConPageSizeInvalidoFalla() {
        loggedAs(USER_ID);

        assertThatThrownBy(() -> service.mine(0, 51))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", "VALIDATION_ERROR");
    }

    // ---- Catálogo ----

    @Test
    @SuppressWarnings("unchecked")
    void buscarUsaOrdenDescendenteYPaginacion() {
        anonymous();
        AdoptionPublication p = existing(owner, AdoptionStatus.AVAILABLE);
        when(adoptionRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(p), org.springframework.data.domain.PageRequest.of(1, 5), 6));
        AdoptionFilterDTO filter = new AdoptionFilterDTO();
        filter.setSpecies(Species.DOG);

        PageDTO<AdoptionSummaryDTO> result = service.search(filter, 1, 5);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(adoptionRepository).findAll(any(Specification.class), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(captor.getValue().getPageSize()).isEqualTo(5);
        assertThat(captor.getValue().getSort())
                .isEqualTo(Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
        assertThat(result.items()).extracting(AdoptionSummaryDTO::getPetName).containsExactly("Original");
        assertThat(result.totalItems()).isEqualTo(6);
        assertThat(result.totalPages()).isEqualTo(2);
    }

    @Test
    void buscarConPaginaNegativaFalla() {
        assertThatThrownBy(() -> service.search(new AdoptionFilterDTO(), -1, 20))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", "VALIDATION_ERROR");
    }
}
