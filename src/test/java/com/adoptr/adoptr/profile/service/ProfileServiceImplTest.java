package com.adoptr.adoptr.profile.service;

import com.adoptr.adoptr.location.model.Locality;
import com.adoptr.adoptr.location.model.Province;
import com.adoptr.adoptr.location.repository.LocalityRepository;
import com.adoptr.adoptr.profile.dto.request.ProfileDTOin;
import com.adoptr.adoptr.profile.dto.response.ProfileDTO;
import com.adoptr.adoptr.profile.model.Gender;
import com.adoptr.adoptr.profile.model.OrganizationProfile;
import com.adoptr.adoptr.profile.model.PersonProfile;
import com.adoptr.adoptr.profile.model.Profile;
import com.adoptr.adoptr.profile.model.ProfileType;
import com.adoptr.adoptr.profile.repository.ProfileRepository;
import com.adoptr.adoptr.profile.service.impl.ProfileServiceImpl;
import com.adoptr.adoptr.shared.exception.custom.BadRequestException;
import com.adoptr.adoptr.shared.exception.custom.ConflictException;
import com.adoptr.adoptr.shared.exception.custom.NotFoundException;
import com.adoptr.adoptr.shared.util.MembershipSupport;
import com.adoptr.adoptr.user.model.User;
import com.adoptr.adoptr.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProfileServiceImplTest {

    private static final long USER_ID = 7L;
    private static final long LOCALITY_ID = 6441030L;

    private final MembershipSupport membershipSupport = mock(MembershipSupport.class);
    private final ProfileRepository profileRepository = mock(ProfileRepository.class);
    private final LocalityRepository localityRepository = mock(LocalityRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final ProfileService service =
            new ProfileServiceImpl(membershipSupport, profileRepository, localityRepository, userRepository);

    private final User user = new User();

    @BeforeEach
    void setUp() {
        user.setId(USER_ID);
        when(membershipSupport.getUserId()).thenReturn(USER_ID);
        when(userRepository.getReferenceById(USER_ID)).thenReturn(user);
        when(localityRepository.findById(LOCALITY_ID)).thenReturn(Optional.of(laPlata()));
        when(profileRepository.saveAndFlush(any(Profile.class))).thenAnswer(invocation -> {
            Profile saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
    }

    private static Locality laPlata() {
        Province province = new Province();
        province.setId(6L);
        province.setName("Buenos Aires");
        Locality locality = new Locality();
        locality.setId(LOCALITY_ID);
        locality.setName("La Plata");
        locality.setDisplayName("La Plata");
        locality.setSearchName("la plata");
        locality.setDepartmentName("La Plata");
        locality.setProvince(province);
        return locality;
    }

    private static ProfileDTOin person() {
        ProfileDTOin request = new ProfileDTOin();
        request.setProfileType(ProfileType.PERSON);
        request.setFirstName("  Dolores ");
        request.setLastName("Dominguez");
        request.setGenderType(Gender.X);
        request.setDescription(" Tengo dos gatos. ");
        request.setLocalityId(LOCALITY_ID);
        return request;
    }

    private static ProfileDTOin organization() {
        ProfileDTOin request = new ProfileDTOin();
        request.setProfileType(ProfileType.ORGANIZATION);
        request.setOrganizationName("Refugio Patitas");
        request.setInstagram("@refugiopatitas");
        request.setFacebook("   ");
        request.setWebsite("https://patitas.org");
        request.setDescription("Rescatamos perros y gatos desde 2015.");
        request.setLocalityId(LOCALITY_ID);
        return request;
    }

    private Profile savedProfile() {
        ArgumentCaptor<Profile> captor = ArgumentCaptor.forClass(Profile.class);
        verify(profileRepository).saveAndFlush(captor.capture());
        return captor.getValue();
    }

    // ---- Alta de persona ----

    @Test
    void creaUnPerfilDePersonaParaElUsuarioDelToken() {
        ProfileDTO result = service.create(person());

        PersonProfile saved = (PersonProfile) savedProfile();
        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getFirstName()).isEqualTo("Dolores");
        assertThat(saved.getDescription()).isEqualTo("Tengo dos gatos.");
        assertThat(saved.getGenderType()).isEqualTo(Gender.X);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getProfileType()).isEqualTo(ProfileType.PERSON);
        assertThat(result.getFirstName()).isEqualTo("Dolores");
        assertThat(result.getLastName()).isEqualTo("Dominguez");
        assertThat(result.getLocality().getDisplayName()).isEqualTo("La Plata");
        assertThat(result.getProvince().getName()).isEqualTo("Buenos Aires");
        assertThat(result.getOrganizationName()).isNull();
    }

    @Test
    void enUnPerfilDePersonaIgnoraLosCamposDeOrganizacion() {
        ProfileDTOin request = person();
        request.setOrganizationName("No corresponde");
        request.setWebsite("no-es-url");

        ProfileDTO result = service.create(request);

        assertThat(savedProfile()).isInstanceOf(PersonProfile.class);
        assertThat(result.getOrganizationName()).isNull();
        assertThat(result.getWebsite()).isNull();
    }

    @Test
    void personaSinNombreApellidoNiGeneroEsInvalida() {
        ProfileDTOin request = person();
        request.setFirstName(" ");
        request.setLastName(null);
        request.setGenderType(null);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", "VALIDATION_ERROR")
                .hasMessageContaining("firstName")
                .hasMessageContaining("lastName")
                .hasMessageContaining("genderType");

        verify(profileRepository, never()).saveAndFlush(any());
    }

    // ---- Alta de organización ----

    @Test
    void creaUnPerfilDeOrganizacion() {
        ProfileDTO result = service.create(organization());

        OrganizationProfile saved = (OrganizationProfile) savedProfile();
        assertThat(saved.getOrganizationName()).isEqualTo("Refugio Patitas");
        assertThat(saved.getFacebook()).as("en blanco se guarda como ausente").isNull();

        assertThat(result.getProfileType()).isEqualTo(ProfileType.ORGANIZATION);
        assertThat(result.getOrganizationName()).isEqualTo("Refugio Patitas");
        assertThat(result.getInstagram()).isEqualTo("@refugiopatitas");
        assertThat(result.getWebsite()).isEqualTo("https://patitas.org");
        assertThat(result.getFirstName()).isNull();
        assertThat(result.getGenderType()).isNull();
    }

    @Test
    void organizacionSinRedesNiWebEsValida() {
        ProfileDTOin request = organization();
        request.setInstagram(null);
        request.setFacebook(null);
        request.setWebsite(null);

        ProfileDTO result = service.create(request);

        assertThat(result.getWebsite()).isNull();
    }

    @Test
    void enUnPerfilDeOrganizacionIgnoraLosCamposDePersona() {
        ProfileDTOin request = organization();
        request.setFirstName("No corresponde");
        request.setGenderType(Gender.FEMALE);

        ProfileDTO result = service.create(request);

        assertThat(savedProfile()).isInstanceOf(OrganizationProfile.class);
        assertThat(result.getFirstName()).isNull();
        assertThat(result.getGenderType()).isNull();
    }

    @Test
    void organizacionSinNombreEsInvalida() {
        ProfileDTOin request = organization();
        request.setOrganizationName("  ");

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("organizationName");
    }

    @ParameterizedTest
    @ValueSource(strings = {"patitas.org", "www.patitas.org", "ftp://patitas.org", "https://"})
    void sitioWebSinHttpEsInvalido(String website) {
        ProfileDTOin request = organization();
        request.setWebsite(website);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("website");
    }

    @Test
    void aceptaSitioWebConHttpEnMayusculas() {
        ProfileDTOin request = organization();
        request.setWebsite("HTTP://patitas.org");

        assertThat(service.create(request).getWebsite()).isEqualTo("HTTP://patitas.org");
    }

    // ---- Reglas comunes ----

    @Test
    void siElUsuarioYaTienePerfilDevuelveConflicto() {
        when(profileRepository.existsByUserId(USER_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.create(person()))
                .isInstanceOf(ConflictException.class)
                .hasFieldOrPropertyWithValue("code", "PROFILE_ALREADY_EXISTS");

        verify(profileRepository, never()).saveAndFlush(any());
    }

    @Test
    void altaSimultaneaQueChocaConLaRestriccionUnicaDevuelveConflicto() {
        when(profileRepository.saveAndFlush(any(Profile.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key user_id"));

        assertThatThrownBy(() -> service.create(person()))
                .isInstanceOf(ConflictException.class)
                .hasFieldOrPropertyWithValue("code", "PROFILE_ALREADY_EXISTS");
    }

    @Test
    void localidadInexistenteEsInvalida() {
        ProfileDTOin request = person();
        request.setLocalityId(1L);
        when(localityRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", "LOCALITY_NOT_FOUND");

        verify(profileRepository, never()).saveAndFlush(any());
    }

    // ---- Lectura ----

    @Test
    void devuelveElPerfilDelUsuarioDelToken() {
        OrganizationProfile organization = new OrganizationProfile();
        organization.setId(3L);
        organization.setOrganizationName("Refugio Patitas");
        organization.setDescription("Rescatamos.");
        organization.setLocality(laPlata());
        when(profileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(organization));

        ProfileDTO result = service.get();

        assertThat(result.getProfileType()).isEqualTo(ProfileType.ORGANIZATION);
        assertThat(result.getOrganizationName()).isEqualTo("Refugio Patitas");
        assertThat(result.getLocality().getId()).isEqualTo(LOCALITY_ID);
    }

    @Test
    void sinPerfilDevuelveNotFound() {
        when(profileRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(service::get)
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", "PROFILE_NOT_FOUND");
    }
}
