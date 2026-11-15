package com.adoptr.adoptr.chat.repository;

import com.adoptr.adoptr.adoption.model.AdoptionPublication;
import com.adoptr.adoptr.adoption.model.AdoptionStatus;
import com.adoptr.adoptr.adoption.model.PetSex;
import com.adoptr.adoptr.adoption.model.PetSize;
import com.adoptr.adoptr.adoption.model.Species;
import com.adoptr.adoptr.adoption.repository.AdoptionPublicationRepository;
import com.adoptr.adoptr.chat.model.ChatMessage;
import com.adoptr.adoptr.chat.model.ChatRole;
import com.adoptr.adoptr.chat.model.Conversation;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ConversationRepositoryTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 11, 15, 12, 0);

    @Autowired
    private ConversationRepository repository;

    @Autowired
    private ChatMessageRepository messageRepository;

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
    private User ana;
    private User beto;

    @BeforeEach
    void setUp() {
        Province province = new Province();
        province.setId(993L);
        province.setName("Provincia de chat");
        provinceRepository.save(province);
        locality = new Locality();
        locality.setId(993_000_001L);
        locality.setName("Localidad de chat");
        locality.setDisplayName("Localidad de chat");
        locality.setSearchName("localidad de chat");
        locality.setProvince(province);
        localityRepository.save(locality);
        owner = user("owner");
        ana = user("ana");
        beto = user("beto");
    }

    private User user(String externalId) {
        User user = new User();
        user.setProvider("google");
        user.setProviderUserId("chat-test-" + externalId);
        user.setEmail(externalId + "@example.com");
        user.setName(externalId);
        return userRepository.save(user);
    }

    private AdoptionPublication publication(User author, String petName) {
        AdoptionPublication p = new AdoptionPublication();
        p.setType(PublicationType.ADOPTION);
        p.setAuthor(author);
        p.setLocality(locality);
        p.setDescription("Muy cariñosa.");
        p.setCreatedAt(T0);
        p.setUpdatedAt(T0);
        p.setPetName(petName);
        p.setSpecies(Species.DOG);
        p.setSex(PetSex.FEMALE);
        p.setSize(PetSize.MEDIUM);
        p.setEstimatedBirthDate(LocalDate.of(2024, 3, 1));
        p.setStatus(AdoptionStatus.AVAILABLE);
        return adoptionRepository.save(p);
    }

    private Conversation conversation(AdoptionPublication publication, User interested, LocalDateTime lastMessageAt) {
        Conversation c = new Conversation();
        c.setPublication(publication);
        c.setOwner(publication.getAuthor());
        c.setInterested(interested);
        c.setCreatedAt(lastMessageAt);
        c.setLastMessageAt(lastMessageAt);
        return repository.saveAndFlush(c);
    }

    private ChatMessage message(Conversation conversation, User sender, String body) {
        ChatMessage m = new ChatMessage();
        m.setConversation(conversation);
        m.setSender(sender);
        m.setBody(body);
        m.setCreatedAt(T0);
        return messageRepository.saveAndFlush(m);
    }

    private List<String> petNames(User user, boolean asOwner, boolean asInterested, Long publicationId) {
        entityManager.flush();
        entityManager.clear();
        return repository.findForUser(user.getId(), asOwner, asInterested, publicationId, PageRequest.of(0, 20))
                .getContent().stream()
                .map(c -> ((AdoptionPublication) c.getPublication()).getPetName())
                .toList();
    }

    @Test
    void hayUnaSolaConversacionPorPublicacionYPersonaInteresada() {
        AdoptionPublication luna = publication(owner, "Luna");
        conversation(luna, ana, T0);

        assertThatThrownBy(() -> conversation(luna, ana, T0.plusHours(1)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void otraPublicacionDelMismoDuenoEsOtraConversacion() {
        AdoptionPublication luna = publication(owner, "Luna");
        AdoptionPublication toto = publication(owner, "Toto");
        Conversation first = conversation(luna, ana, T0);
        Conversation second = conversation(toto, ana, T0);

        assertThat(first.getId()).isNotEqualTo(second.getId());
        assertThat(repository.findByPublicationIdAndInterestedId(toto.getId(), ana.getId()))
                .map(Conversation::getId).contains(second.getId());
    }

    @Test
    void laBandejaSeFiltraPorRolYPublicacion() {
        AdoptionPublication luna = publication(owner, "Luna");
        AdoptionPublication michi = publication(ana, "Michi");
        conversation(luna, ana, T0);              // a Ana: la contactó a owner (INTERESTED)
        conversation(michi, beto, T0.plusHours(1)); // a Ana: la contactaron (OWNER)

        assertThat(petNames(ana, true, true, null)).containsExactly("Michi", "Luna");
        assertThat(petNames(ana, true, false, null)).containsExactly("Michi");
        assertThat(petNames(ana, false, true, null)).containsExactly("Luna");
        assertThat(petNames(ana, true, true, luna.getId())).containsExactly("Luna");
        assertThat(petNames(owner, true, true, null)).containsExactly("Luna");
        assertThat(petNames(beto, true, false, null)).isEmpty();
    }

    @Test
    void laBandejaOrdenaPorActividadMasReciente() {
        AdoptionPublication luna = publication(owner, "Luna");
        AdoptionPublication toto = publication(owner, "Toto");
        conversation(luna, ana, T0.plusDays(1));
        conversation(toto, ana, T0);

        assertThat(petNames(owner, true, false, null)).containsExactly("Luna", "Toto");
    }

    @Test
    void cuentaLosNoLeidosDeLaOtraPersonaDesdeLaMarcaDeLectura() {
        Conversation c = conversation(publication(owner, "Luna"), ana, T0);
        ChatMessage hola = message(c, ana, "Hola");
        message(c, ana, "¿Sigue en adopción?");
        message(c, owner, "¡Sí!");
        message(c, ana, "¡Genial!");
        c.markRead(ChatRole.OWNER, hola.getId());
        repository.saveAndFlush(c);
        entityManager.clear();

        assertThat(messageRepository.countUnread(List.of(c.getId()), owner.getId()))
                .containsExactly(new UnreadCount(c.getId(), 2L));
        // Ana nunca marcó lectura: el único mensaje ajeno es el del dueño.
        assertThat(messageRepository.countUnread(List.of(c.getId()), ana.getId()))
                .containsExactly(new UnreadCount(c.getId(), 1L));
    }

    @Test
    void traeElUltimoMensajeDeCadaConversacion() {
        Conversation luna = conversation(publication(owner, "Luna"), ana, T0);
        Conversation toto = conversation(publication(owner, "Toto"), ana, T0);
        message(luna, ana, "Hola Luna");
        ChatMessage lastLuna = message(luna, owner, "Respuesta Luna");
        ChatMessage lastToto = message(toto, ana, "Hola Toto");
        entityManager.clear();

        assertThat(messageRepository.findLastMessages(List.of(luna.getId(), toto.getId())))
                .extracting(ChatMessage::getId)
                .containsExactlyInAnyOrder(lastLuna.getId(), lastToto.getId());
        assertThat(messageRepository.findLastMessageId(luna.getId())).contains(lastLuna.getId());
    }

    @Test
    void elHistorialSePaginaHaciaAtrasConCursor() {
        Conversation c = conversation(publication(owner, "Luna"), ana, T0);
        ChatMessage m1 = message(c, ana, "1");
        ChatMessage m2 = message(c, owner, "2");
        ChatMessage m3 = message(c, ana, "3");
        PageRequest two = PageRequest.of(0, 2, Sort.by(Sort.Order.desc("id")));

        Slice<ChatMessage> latest = messageRepository.findByConversationId(c.getId(), two);
        Slice<ChatMessage> older = messageRepository.findByConversationIdAndIdLessThan(c.getId(), m2.getId(), two);

        assertThat(latest.getContent()).extracting(ChatMessage::getId).containsExactly(m3.getId(), m2.getId());
        assertThat(latest.hasNext()).isTrue();
        assertThat(older.getContent()).extracting(ChatMessage::getId).containsExactly(m1.getId());
        assertThat(older.hasNext()).isFalse();
    }

    @Test
    void laConversacionTraeLaPublicacionConSuSubtipo() {
        Conversation c = conversation(publication(owner, "Luna"), ana, T0);
        entityManager.flush();
        entityManager.clear();

        Conversation read = repository.findWithPublicationById(c.getId()).orElseThrow();

        assertThat(read.getPublication()).isInstanceOf(AdoptionPublication.class);
    }
}
