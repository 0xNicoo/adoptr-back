package com.adoptr.adoptr.adoption.repository;

import com.adoptr.adoptr.adoption.model.AdoptionPublication;
import com.adoptr.adoptr.adoption.model.AdoptionStatus;
import com.adoptr.adoptr.adoption.model.PetSex;
import com.adoptr.adoptr.adoption.model.PetSize;
import com.adoptr.adoptr.adoption.model.Species;
import com.adoptr.adoptr.location.model.Locality;
import com.adoptr.adoptr.location.model.Province;
import com.adoptr.adoptr.location.repository.LocalityRepository;
import com.adoptr.adoptr.location.repository.ProvinceRepository;
import com.adoptr.adoptr.publication.model.PublicationType;
import com.adoptr.adoptr.user.model.User;
import com.adoptr.adoptr.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Persistencia de publicaciones de adopción contra PostgreSQL real
 * (como ProfileRepositoryTest, necesita la base y las variables de entorno).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AdoptionPublicationRepositoryTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 11, 7, 12, 0);
    private static final Sort NEWEST_FIRST = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

    @Autowired
    private AdoptionPublicationRepository repository;

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
    private User other;

    @BeforeEach
    void setUp() {
        Province province = new Province();
        province.setId(996L);
        province.setName("Provincia de prueba");
        provinceRepository.save(province);
        locality = new Locality();
        locality.setId(996_000_001L);
        locality.setName("Localidad de prueba");
        locality.setDisplayName("Localidad de prueba");
        locality.setSearchName("localidad de prueba");
        locality.setProvince(province);
        localityRepository.save(locality);
        owner = user("owner");
        other = user("other");
    }

    private User user(String externalId) {
        User user = new User();
        user.setProvider("google");
        user.setProviderUserId("adoption-test-" + externalId);
        user.setEmail(externalId + "@example.com");
        user.setName(externalId);
        return userRepository.save(user);
    }

    private AdoptionPublication publication(User author, String petName, AdoptionStatus status, LocalDateTime createdAt) {
        AdoptionPublication p = new AdoptionPublication();
        p.setType(PublicationType.ADOPTION);
        p.setAuthor(author);
        p.setLocality(locality);
        p.setDescription("Muy cariñosa.");
        p.setCreatedAt(createdAt);
        p.setUpdatedAt(createdAt);
        p.setPetName(petName);
        p.setSpecies(Species.DOG);
        p.setSex(PetSex.FEMALE);
        p.setSize(PetSize.MEDIUM);
        p.setEstimatedBirthDate(LocalDate.of(2024, 3, 1));
        p.setVaccinated(true);
        p.setDewormed(true);
        p.setNeutered(false);
        p.setStatus(status);
        return repository.save(p);
    }

    @Test
    void guardaYLeeConHerenciaJoined() {
        Long id = publication(owner, "Luna", AdoptionStatus.AVAILABLE, T0).getId();
        entityManager.flush();
        entityManager.clear();

        AdoptionPublication read = repository.findById(id).orElseThrow();

        assertThat(read.getType()).isEqualTo(PublicationType.ADOPTION);
        assertThat(read.getAuthor().getId()).isEqualTo(owner.getId());
        assertThat(read.getLocality().getProvince().getId()).isEqualTo(996L);
        assertThat(read.getDescription()).isEqualTo("Muy cariñosa.");
        assertThat(read.getCreatedAt()).isEqualTo(T0);
        assertThat(read.getUpdatedAt()).isEqualTo(T0);
        assertThat(read.getDeletedAt()).isNull();
        assertThat(read.getPetName()).isEqualTo("Luna");
        assertThat(read.getSpecies()).isEqualTo(Species.DOG);
        assertThat(read.getSex()).isEqualTo(PetSex.FEMALE);
        assertThat(read.getSize()).isEqualTo(PetSize.MEDIUM);
        assertThat(read.getEstimatedBirthDate()).isEqualTo(LocalDate.of(2024, 3, 1));
        assertThat(read.isVaccinated()).isTrue();
        assertThat(read.isDewormed()).isTrue();
        assertThat(read.isNeutered()).isFalse();
        assertThat(read.getStatus()).isEqualTo(AdoptionStatus.AVAILABLE);
    }

    @Test
    void findByIdIgnoraBorradas() {
        AdoptionPublication deleted = publication(owner, "Borrada", AdoptionStatus.AVAILABLE, T0);
        deleted.setDeletedAt(T0.plusDays(1));
        repository.save(deleted);

        assertThat(repository.findByIdAndDeletedAtIsNull(deleted.getId())).isEmpty();
    }

    @Test
    void mineDevuelveTodosLosEstadosSinBorradasNiAjenas() {
        publication(owner, "Vieja", AdoptionStatus.ADOPTED, T0);
        publication(owner, "Nueva", AdoptionStatus.AVAILABLE, T0.plusDays(1));
        AdoptionPublication deleted = publication(owner, "Borrada", AdoptionStatus.AVAILABLE, T0.plusDays(2));
        deleted.setDeletedAt(T0.plusDays(3));
        repository.save(deleted);
        publication(other, "Ajena", AdoptionStatus.AVAILABLE, T0.plusDays(4));
        entityManager.flush();
        entityManager.clear();

        Page<AdoptionPublication> page = repository.findByAuthorIdAndDeletedAtIsNull(
                owner.getId(), PageRequest.of(0, 20, NEWEST_FIRST));

        assertThat(page.getContent()).extracting(AdoptionPublication::getPetName).containsExactly("Nueva", "Vieja");
        assertThat(page.getTotalElements()).isEqualTo(2);
    }
}
