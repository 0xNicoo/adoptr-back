package com.adoptr.adoptr.publication.repository;

import com.adoptr.adoptr.adoption.model.AdoptionPublication;
import com.adoptr.adoptr.adoption.model.AdoptionStatus;
import com.adoptr.adoptr.adoption.model.PetSex;
import com.adoptr.adoptr.adoption.model.Species;
import com.adoptr.adoptr.adoption.repository.AdoptionPublicationRepository;
import com.adoptr.adoptr.publication.model.PublicationPhoto;
import com.adoptr.adoptr.publication.model.PublicationType;
import com.adoptr.adoptr.shared.storage.ProcessedImage;
import com.adoptr.adoptr.user.model.User;
import com.adoptr.adoptr.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Fotos de publicaciones contra PostgreSQL real (como AdoptionPublicationRepositoryTest). */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PublicationPhotoRepositoryTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 11, 8, 12, 0);

    @Autowired
    private PublicationPhotoRepository repository;

    @Autowired
    private AdoptionPublicationRepository adoptionRepository;

    @Autowired
    private UserRepository userRepository;

    private AdoptionPublication publication;
    private AdoptionPublication other;

    @BeforeEach
    void setUp() {
        User author = new User();
        author.setProvider("google");
        author.setProviderUserId("fotos-" + UUID.randomUUID());
        author = userRepository.save(author);
        publication = adoptionRepository.save(publication(author));
        other = adoptionRepository.save(publication(author));
    }

    private static AdoptionPublication publication(User author) {
        AdoptionPublication p = new AdoptionPublication();
        p.setType(PublicationType.ADOPTION);
        p.setAuthor(author);
        p.setDescription("Descripción");
        p.setPetName("Luna");
        p.setSpecies(Species.CAT);
        p.setSex(PetSex.FEMALE);
        p.setEstimatedBirthDate(LocalDate.of(2024, 3, 1));
        p.setStatus(AdoptionStatus.AVAILABLE);
        p.setCreatedAt(T0);
        p.setUpdatedAt(T0);
        return p;
    }

    private PublicationPhoto photo(AdoptionPublication owner, int sortOrder) {
        PublicationPhoto photo = new PublicationPhoto();
        photo.setPublication(owner);
        photo.setSortOrder(sortOrder);
        photo.fillFrom("test/" + UUID.randomUUID() + ".jpg", new ProcessedImage(new byte[3], "image/jpeg", "jpg"), T0);
        return photo;
    }

    @Test
    void devuelveLasFotosOrdenadas() {
        PublicationPhoto second = repository.saveAndFlush(photo(publication, 4));
        PublicationPhoto first = repository.saveAndFlush(photo(publication, 1));
        repository.saveAndFlush(photo(other, 0));

        assertThat(repository.findByPublicationIdOrderBySortOrderAsc(publication.getId()))
                .extracting(PublicationPhoto::getId)
                .containsExactly(first.getId(), second.getId());
    }

    @Test
    void traeLasFotosDeVariasPublicaciones() {
        repository.saveAndFlush(photo(publication, 0));
        repository.saveAndFlush(photo(other, 0));

        assertThat(repository.findByPublicationIdInOrderBySortOrderAsc(List.of(publication.getId(), other.getId())))
                .hasSize(2);
    }

    @Test
    void noPermiteDosFotosEnElMismoLugar() {
        repository.saveAndFlush(photo(publication, 0));

        assertThatThrownBy(() -> repository.saveAndFlush(photo(publication, 0)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void buscaLaFotoSoloDentroDeSuPublicacion() {
        PublicationPhoto saved = repository.saveAndFlush(photo(publication, 0));

        assertThat(repository.findByIdAndPublicationId(saved.getId(), publication.getId())).isPresent();
        assertThat(repository.findByIdAndPublicationId(saved.getId(), other.getId())).isEmpty();
    }
}
