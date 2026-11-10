package com.adoptr.adoptr.adoption.repository;

import com.adoptr.adoptr.adoption.dto.request.AdoptionFilterDTO;
import com.adoptr.adoptr.adoption.model.AdoptionPublication;
import com.adoptr.adoptr.adoption.model.AdoptionStatus;
import com.adoptr.adoptr.adoption.model.LifeStage;
import com.adoptr.adoptr.adoption.model.PetSex;
import com.adoptr.adoptr.adoption.model.PetSize;
import com.adoptr.adoptr.adoption.model.Species;
import com.adoptr.adoptr.adoption.spec.AdoptionSpec;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.function.Consumer;

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
    private Locality otherProvinceLocality;
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
        Province otherProvince = new Province();
        otherProvince.setId(995L);
        otherProvince.setName("Otra provincia de prueba");
        provinceRepository.save(otherProvince);
        otherProvinceLocality = new Locality();
        otherProvinceLocality.setId(995_000_001L);
        otherProvinceLocality.setName("Otra localidad");
        otherProvinceLocality.setDisplayName("Otra localidad");
        otherProvinceLocality.setSearchName("otra localidad");
        otherProvinceLocality.setProvince(otherProvince);
        localityRepository.save(otherProvinceLocality);
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

    @Test
    void mineFiltraPorEstadoSinBorradasNiAjenas() {
        publication(owner, "Adoptada", AdoptionStatus.ADOPTED, T0);
        publication(owner, "Disponible vieja", AdoptionStatus.AVAILABLE, T0.plusDays(1));
        publication(owner, "Disponible nueva", AdoptionStatus.AVAILABLE, T0.plusDays(2));
        AdoptionPublication deleted = publication(owner, "Borrada", AdoptionStatus.AVAILABLE, T0.plusDays(3));
        deleted.setDeletedAt(T0.plusDays(4));
        repository.save(deleted);
        publication(other, "Ajena", AdoptionStatus.AVAILABLE, T0.plusDays(5));
        entityManager.flush();
        entityManager.clear();

        Page<AdoptionPublication> available = repository.findByAuthorIdAndStatusAndDeletedAtIsNull(
                owner.getId(), AdoptionStatus.AVAILABLE, PageRequest.of(0, 20, NEWEST_FIRST));
        Page<AdoptionPublication> adopted = repository.findByAuthorIdAndStatusAndDeletedAtIsNull(
                owner.getId(), AdoptionStatus.ADOPTED, PageRequest.of(0, 20, NEWEST_FIRST));

        assertThat(available.getContent()).extracting(AdoptionPublication::getPetName)
                .containsExactly("Disponible nueva", "Disponible vieja");
        assertThat(available.getTotalElements()).isEqualTo(2);
        assertThat(adopted.getContent()).extracting(AdoptionPublication::getPetName).containsExactly("Adoptada");
        AdoptionPublication read = available.getContent().getFirst();
        assertThat(Hibernate.isInitialized(read.getLocality().getProvince())).isTrue();
        assertThat(Hibernate.isInitialized(read.getAuthor())).isFalse();
    }

    @Test
    void mineTraeUbicacionPeroNoCargaAutorNiPerfil() {
        publication(owner, "Luna", AdoptionStatus.AVAILABLE, T0);
        entityManager.flush();
        entityManager.clear();

        AdoptionPublication read = repository.findByAuthorIdAndDeletedAtIsNull(
                owner.getId(), PageRequest.of(0, 20, NEWEST_FIRST)).getContent().getFirst();

        assertThat(Hibernate.isInitialized(read.getLocality())).isTrue();
        assertThat(Hibernate.isInitialized(read.getLocality().getProvince())).isTrue();
        assertThat(Hibernate.isInitialized(read.getAuthor())).isFalse();
        assertThat(read.getAuthor().getId()).isEqualTo(owner.getId());
    }

    // ---- Catálogo ----

    private static final YearMonth CURRENT = YearMonth.of(2026, 11);

    private AdoptionPublication available(String petName, Consumer<AdoptionPublication> tweak) {
        AdoptionPublication p = publication(owner, petName, AdoptionStatus.AVAILABLE, T0);
        tweak.accept(p);
        return repository.save(p);
    }

    private List<String> catalog(AdoptionFilterDTO filter) {
        entityManager.flush();
        entityManager.clear();
        return repository.findAll(AdoptionSpec.catalog(filter, CURRENT), PageRequest.of(0, 50, NEWEST_FIRST))
                .map(AdoptionPublication::getPetName).getContent();
    }

    private static AdoptionFilterDTO filter(Consumer<AdoptionFilterDTO> setup) {
        AdoptionFilterDTO filter = new AdoptionFilterDTO();
        setup.accept(filter);
        return filter;
    }

    @Test
    void catalogoExcluyeAdoptadasYBorradas() {
        available("Disponible", p -> { });
        available("Adoptada", p -> p.setStatus(AdoptionStatus.ADOPTED));
        available("Borrada", p -> p.setDeletedAt(T0.plusDays(1)));

        assertThat(catalog(new AdoptionFilterDTO())).containsExactly("Disponible");
    }

    @Test
    void filtraPorEspecie() {
        available("Perro", p -> { });
        available("Gato", p -> { p.setSpecies(Species.CAT); p.setSize(null); });

        assertThat(catalog(filter(f -> f.setSpecies(Species.CAT)))).containsExactly("Gato");
    }

    @Test
    void filtraPorSexo() {
        available("Hembra", p -> { });
        available("Macho", p -> p.setSex(PetSex.MALE));

        assertThat(catalog(filter(f -> f.setSex(PetSex.MALE)))).containsExactly("Macho");
    }

    @Test
    void filtraPorTamano() {
        available("Mediano", p -> { });
        available("Grande", p -> p.setSize(PetSize.LARGE));

        assertThat(catalog(filter(f -> f.setSize(PetSize.LARGE)))).containsExactly("Grande");
    }

    @Test
    void filtraPorProvincia() {
        available("Aca", p -> { });
        available("Alla", p -> p.setLocality(otherProvinceLocality));

        assertThat(catalog(filter(f -> f.setProvinceId(995L)))).containsExactly("Alla");
    }

    @Test
    void filtraPorLocalidad() {
        available("Aca", p -> { });
        available("Alla", p -> p.setLocality(otherProvinceLocality));

        assertThat(catalog(filter(f -> f.setLocalityId(996_000_001L)))).containsExactly("Aca");
    }

    @Test
    void filtraPorEtapaDeVida() {
        available("Cachorro", p -> p.setEstimatedBirthDate(LocalDate.of(2025, 12, 1)));
        available("Adulto", p -> p.setEstimatedBirthDate(LocalDate.of(2025, 11, 1)));
        available("AdultoMayorLimite", p -> p.setEstimatedBirthDate(LocalDate.of(2018, 12, 1)));
        available("Mayor", p -> p.setEstimatedBirthDate(LocalDate.of(2018, 11, 1)));

        assertThat(catalog(filter(f -> f.setLifeStage(LifeStage.PUPPY)))).containsExactly("Cachorro");
        assertThat(catalog(filter(f -> f.setLifeStage(LifeStage.ADULT))))
                .containsExactlyInAnyOrder("Adulto", "AdultoMayorLimite");
        assertThat(catalog(filter(f -> f.setLifeStage(LifeStage.SENIOR)))).containsExactly("Mayor");
    }

    @Test
    void combinaFiltros() {
        available("Buscada", p -> p.setSex(PetSex.MALE));
        available("OtroSexo", p -> { });
        available("OtraProvincia", p -> { p.setSex(PetSex.MALE); p.setLocality(otherProvinceLocality); });

        assertThat(catalog(filter(f -> {
            f.setSpecies(Species.DOG);
            f.setSex(PetSex.MALE);
            f.setProvinceId(996L);
        }))).containsExactly("Buscada");
    }

    @Test
    void localidadDeOtraProvinciaDevuelveVacio() {
        available("Aca", p -> { });

        assertThat(catalog(filter(f -> {
            f.setProvinceId(995L);
            f.setLocalityId(996_000_001L);
        }))).isEmpty();
    }

    @Test
    void tamanoConGatoDevuelveVacio() {
        available("Gato", p -> { p.setSpecies(Species.CAT); p.setSize(null); });

        assertThat(catalog(filter(f -> {
            f.setSpecies(Species.CAT);
            f.setSize(PetSize.SMALL);
        }))).isEmpty();
    }

    @Test
    void catalogoNoCargaAutorNiPerfil() {
        available("Luna", p -> { });
        entityManager.flush();
        entityManager.clear();

        AdoptionPublication read = repository.findAll(AdoptionSpec.catalog(new AdoptionFilterDTO(), CURRENT),
                PageRequest.of(0, 20, NEWEST_FIRST)).getContent().getFirst();

        assertThat(Hibernate.isInitialized(read.getLocality())).isTrue();
        assertThat(Hibernate.isInitialized(read.getLocality().getProvince())).isTrue();
        assertThat(Hibernate.isInitialized(read.getAuthor())).isFalse();
    }

    @Test
    void paginaFueraDeRangoDevuelveVacia() {
        available("Uno", p -> { });
        available("Dos", p -> { });
        available("Tres", p -> { });
        entityManager.flush();
        entityManager.clear();

        Page<AdoptionPublication> page = repository.findAll(AdoptionSpec.catalog(new AdoptionFilterDTO(), CURRENT),
                PageRequest.of(5, 2, NEWEST_FIRST));

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    void ordenDescendentePorFechaYId() {
        available("Vieja", p -> p.setCreatedAt(T0.minusDays(1)));
        available("EmpatePrimera", p -> { });
        available("EmpateSegunda", p -> { });
        available("Nueva", p -> p.setCreatedAt(T0.plusDays(1)));

        assertThat(catalog(new AdoptionFilterDTO()))
                .containsExactly("Nueva", "EmpateSegunda", "EmpatePrimera", "Vieja");
    }
}
