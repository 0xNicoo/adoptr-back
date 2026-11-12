package com.adoptr.adoptr.favorite.repository;

import com.adoptr.adoptr.adoption.model.AdoptionPublication;
import com.adoptr.adoptr.adoption.model.AdoptionStatus;
import com.adoptr.adoptr.adoption.model.PetSex;
import com.adoptr.adoptr.adoption.model.PetSize;
import com.adoptr.adoptr.adoption.model.Species;
import com.adoptr.adoptr.adoption.repository.AdoptionPublicationRepository;
import com.adoptr.adoptr.favorite.model.Favorite;
import com.adoptr.adoptr.location.model.Locality;
import com.adoptr.adoptr.location.model.Province;
import com.adoptr.adoptr.location.repository.LocalityRepository;
import com.adoptr.adoptr.location.repository.ProvinceRepository;
import com.adoptr.adoptr.publication.model.PublicationType;
import com.adoptr.adoptr.user.model.User;
import com.adoptr.adoptr.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FavoriteRepositoryTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 11, 7, 12, 0);

    @Autowired
    private FavoriteRepository repository;

    @Autowired
    private AdoptionPublicationRepository adoptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProvinceRepository provinceRepository;

    @Autowired
    private LocalityRepository localityRepository;

    @Autowired
    private EntityManager entityManager;

    private Locality locality;
    private User owner;
    private User viewer;
    private User other;

    @BeforeEach
    void setUp() {
        Province province = new Province();
        province.setId(994L);
        province.setName("Provincia de favoritos");
        provinceRepository.save(province);
        locality = new Locality();
        locality.setId(994_000_001L);
        locality.setName("Localidad de favoritos");
        locality.setDisplayName("Localidad de favoritos");
        locality.setSearchName("localidad de favoritos");
        locality.setProvince(province);
        localityRepository.save(locality);
        owner = user("owner");
        viewer = user("viewer");
        other = user("other");
    }

    private User user(String externalId) {
        User user = new User();
        user.setProvider("google");
        user.setProviderUserId("favorite-test-" + externalId);
        user.setEmail(externalId + "@example.com");
        user.setName(externalId);
        return userRepository.save(user);
    }

    private AdoptionPublication publication(String petName, Species species, LocalDateTime createdAt) {
        AdoptionPublication p = new AdoptionPublication();
        p.setType(PublicationType.ADOPTION);
        p.setAuthor(owner);
        p.setLocality(locality);
        p.setDescription("Muy cariñosa.");
        p.setCreatedAt(createdAt);
        p.setUpdatedAt(createdAt);
        p.setPetName(petName);
        p.setSpecies(species);
        p.setSex(PetSex.FEMALE);
        p.setSize(species == Species.DOG ? PetSize.MEDIUM : null);
        p.setEstimatedBirthDate(LocalDate.of(2024, 3, 1));
        p.setVaccinated(true);
        p.setDewormed(true);
        p.setNeutered(false);
        p.setStatus(AdoptionStatus.AVAILABLE);
        return adoptionRepository.save(p);
    }

    private Favorite favorite(User user, AdoptionPublication publication, LocalDateTime createdAt) {
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setPublication(publication);
        favorite.setCreatedAt(createdAt);
        return repository.saveAndFlush(favorite);
    }

    private Page<AdoptionPublication> adoptions(User user, Species species) {
        entityManager.flush();
        entityManager.clear();
        return repository.findAdoptions(user.getId(), species, PageRequest.of(0, 12));
    }

    @Test
    void noPermiteGuardarDosVecesLaMismaPublicacion() {
        AdoptionPublication luna = publication("Luna", Species.DOG, T0);
        favorite(viewer, luna, T0);

        assertThatThrownBy(() -> favorite(viewer, luna, T0.plusHours(1)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void listaPrimeroLaGuardadaMasReciente() {
        AdoptionPublication luna = publication("Luna", Species.DOG, T0.plusDays(1));
        AdoptionPublication toto = publication("Toto", Species.DOG, T0);
        favorite(viewer, luna, T0);
        favorite(viewer, toto, T0.plusHours(1));

        Page<AdoptionPublication> page = adoptions(viewer, null);

        assertThat(page.getContent()).extracting(AdoptionPublication::getPetName).containsExactly("Toto", "Luna");
    }

    @Test
    void filtraPorEspecie() {
        favorite(viewer, publication("Luna", Species.DOG, T0), T0);
        favorite(viewer, publication("Michi", Species.CAT, T0), T0.plusHours(1));

        Page<AdoptionPublication> cats = adoptions(viewer, Species.CAT);
        Page<AdoptionPublication> all = adoptions(viewer, null);

        assertThat(cats.getContent()).extracting(AdoptionPublication::getPetName).containsExactly("Michi");
        assertThat(cats.getTotalElements()).isEqualTo(1);
        assertThat(all.getTotalElements()).isEqualTo(2);
    }

    @Test
    void excluyeLasPublicacionesDadasDeBaja() {
        AdoptionPublication luna = publication("Luna", Species.DOG, T0);
        AdoptionPublication toto = publication("Toto", Species.DOG, T0);
        favorite(viewer, luna, T0);
        favorite(viewer, toto, T0);
        toto.setDeletedAt(T0.plusDays(1));
        adoptionRepository.saveAndFlush(toto);

        assertThat(adoptions(viewer, null).getContent()).extracting(AdoptionPublication::getPetName).containsExactly("Luna");
        assertThat(repository.findPublicationIds(viewer.getId())).containsExactly(luna.getId());
    }

    @Test
    void ignoraLosFavoritosDeOtrosUsuarios() {
        AdoptionPublication luna = publication("Luna", Species.DOG, T0);
        favorite(other, luna, T0);

        assertThat(adoptions(viewer, null).getContent()).isEmpty();
        assertThat(repository.findPublicationIds(viewer.getId())).isEmpty();
    }

    @Test
    void traeLocalidadYProvinciaEnLaMismaConsulta() {
        favorite(viewer, publication("Luna", Species.DOG, T0), T0);

        AdoptionPublication read = adoptions(viewer, null).getContent().getFirst();

        assertThat(Hibernate.isInitialized(read.getLocality())).isTrue();
        assertThat(Hibernate.isInitialized(read.getLocality().getProvince())).isTrue();
    }

    @Test
    void borrarDevuelveCuantosQuito() {
        AdoptionPublication luna = publication("Luna", Species.DOG, T0);
        favorite(viewer, luna, T0);

        assertThat(repository.deleteByUserIdAndPublicationId(viewer.getId(), luna.getId())).isEqualTo(1);
        assertThat(repository.deleteByUserIdAndPublicationId(viewer.getId(), luna.getId())).isZero();
        assertThat(repository.existsByUserIdAndPublicationId(viewer.getId(), luna.getId())).isFalse();
    }
}
