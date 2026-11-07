package com.adoptr.adoptr.profile.repository;

import com.adoptr.adoptr.location.model.Locality;
import com.adoptr.adoptr.location.model.Province;
import com.adoptr.adoptr.location.repository.LocalityRepository;
import com.adoptr.adoptr.location.repository.ProvinceRepository;
import com.adoptr.adoptr.profile.model.Gender;
import com.adoptr.adoptr.profile.model.OrganizationProfile;
import com.adoptr.adoptr.profile.model.PersonProfile;
import com.adoptr.adoptr.profile.model.Profile;
import com.adoptr.adoptr.profile.model.ProfileType;
import com.adoptr.adoptr.user.model.User;
import com.adoptr.adoptr.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Prueba la herencia JOINED y la restricción de un perfil por usuario contra
 * PostgreSQL real (como AdoptrApplicationTests, necesita la base y las variables
 * de entorno). Cada test se revierte al terminar.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProfileRepositoryTest {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProvinceRepository provinceRepository;

    @Autowired
    private LocalityRepository localityRepository;

    @Autowired
    private EntityManager entityManager;

    private Locality locality;

    @BeforeEach
    void setUp() {
        Province province = new Province();
        province.setId(997L);
        province.setName("Provincia de prueba");
        provinceRepository.save(province);
        locality = new Locality();
        locality.setId(997_000_001L);
        locality.setName("Localidad de prueba");
        locality.setDisplayName("Localidad de prueba");
        locality.setSearchName("localidad de prueba");
        locality.setDepartmentName("Test");
        locality.setProvince(province);
        localityRepository.save(locality);
    }

    private User user(String providerUserId) {
        User user = new User();
        user.setProvider("google");
        user.setProviderUserId(providerUserId);
        user.setEmail(providerUserId + "@test.local");
        user.setName("Test");
        return userRepository.save(user);
    }

    private PersonProfile person(User user) {
        PersonProfile profile = new PersonProfile();
        profile.setUser(user);
        profile.setFirstName("Dolores");
        profile.setLastName("Dominguez");
        profile.setGenderType(Gender.X);
        profile.setDescription("Tengo dos gatos.");
        profile.setLocality(locality);
        return profile;
    }

    @Test
    void guardaYRecuperaCadaTipoConSusCampos() {
        User personUser = user("test-person");
        User organizationUser = user("test-organization");
        profileRepository.saveAndFlush(person(personUser));
        OrganizationProfile organization = new OrganizationProfile();
        organization.setUser(organizationUser);
        organization.setOrganizationName("Refugio Patitas");
        organization.setWebsite("https://patitas.org");
        organization.setDescription("Rescatamos.");
        organization.setLocality(locality);
        profileRepository.saveAndFlush(organization);
        entityManager.clear();

        Profile foundPerson = profileRepository.findByUserId(personUser.getId()).orElseThrow();
        Profile foundOrganization = profileRepository.findByUserId(organizationUser.getId()).orElseThrow();

        assertThat(foundPerson).isInstanceOf(PersonProfile.class);
        assertThat(foundPerson.getProfileType()).isEqualTo(ProfileType.PERSON);
        assertThat(((PersonProfile) foundPerson).getGenderType()).isEqualTo(Gender.X);
        assertThat(foundOrganization).isInstanceOf(OrganizationProfile.class);
        assertThat(((OrganizationProfile) foundOrganization).getWebsite()).isEqualTo("https://patitas.org");
        assertThat(foundOrganization.getLocality().getId()).isEqualTo(997_000_001L);
        assertThat(profileRepository.existsByUserId(personUser.getId())).isTrue();
    }

    @Test
    void laBaseImpideDosPerfilesParaElMismoUsuario() {
        User user = user("test-duplicate");
        profileRepository.saveAndFlush(person(user));

        assertThatThrownBy(() -> profileRepository.saveAndFlush(person(user)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
