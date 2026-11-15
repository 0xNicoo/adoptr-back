package com.adoptr.adoptr.chat.controller;

import com.adoptr.adoptr.adoption.model.AdoptionPublication;
import com.adoptr.adoptr.adoption.model.AdoptionStatus;
import com.adoptr.adoptr.adoption.model.Species;
import com.adoptr.adoptr.adoption.repository.AdoptionPublicationRepository;
import com.adoptr.adoptr.chat.model.ChatMessage;
import com.adoptr.adoptr.chat.model.Conversation;
import com.adoptr.adoptr.chat.repository.ChatMessageRepository;
import com.adoptr.adoptr.chat.repository.ConversationRepository;
import com.adoptr.adoptr.chat.repository.UnreadCount;
import com.adoptr.adoptr.chat.service.impl.ConversationAssembler;
import com.adoptr.adoptr.chat.service.impl.ConversationServiceImpl;
import com.adoptr.adoptr.profile.model.OrganizationProfile;
import com.adoptr.adoptr.profile.model.PersonProfile;
import com.adoptr.adoptr.profile.repository.ProfileRepository;
import com.adoptr.adoptr.publication.model.PublicationType;
import com.adoptr.adoptr.publication.repository.PublicationPhotoRepository;
import com.adoptr.adoptr.shared.config.ClockConfig;
import com.adoptr.adoptr.shared.security.JwtAuthFilter;
import com.adoptr.adoptr.shared.security.JwtUtil;
import com.adoptr.adoptr.shared.security.SecurityConfig;
import com.adoptr.adoptr.shared.storage.ImageStorage;
import com.adoptr.adoptr.shared.util.MembershipSupport;
import com.adoptr.adoptr.user.model.User;
import com.adoptr.adoptr.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ConversationController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtUtil.class, ConversationServiceImpl.class,
        ConversationAssembler.class, MembershipSupport.class, ConversationControllerWebTest.FixedClock.class})
@TestPropertySource(properties = {
        "security.jwt.secret=test-secret-test-secret-test-secret-123456",
        "security.jwt.expiration=3600000"
})
class ConversationControllerWebTest {

    private static final long OWNER_ID = 7L;
    private static final long INTERESTED_ID = 8L;
    private static final long STRANGER_ID = 9L;
    private static final long CONVERSATION_ID = 50L;
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 15, 12, 0);

    @TestConfiguration
    static class FixedClock {
        @Bean
        Clock clock() {
            return Clock.fixed(Instant.parse("2026-11-15T15:00:00Z"), ClockConfig.ZONE);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private ConversationRepository conversationRepository;

    @MockitoBean
    private ChatMessageRepository messageRepository;

    @MockitoBean
    private AdoptionPublicationRepository adoptionRepository;

    @MockitoBean
    private ProfileRepository profileRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PublicationPhotoRepository photoRepository;

    @MockitoBean
    private ImageStorage imageStorage;

    private User owner;
    private User interested;
    private AdoptionPublication luna;
    private Conversation conversation;
    private final AtomicLong messageIds = new AtomicLong(100);

    @BeforeEach
    void setUp() {
        owner = user(OWNER_ID);
        interested = user(INTERESTED_ID);
        luna = new AdoptionPublication();
        luna.setId(1L);
        luna.setType(PublicationType.ADOPTION);
        luna.setAuthor(owner);
        luna.setPetName("Luna");
        luna.setSpecies(Species.DOG);
        luna.setEstimatedBirthDate(LocalDate.of(2024, 3, 1));
        luna.setStatus(AdoptionStatus.AVAILABLE);

        conversation = new Conversation();
        conversation.setId(CONVERSATION_ID);
        conversation.setPublication(luna);
        conversation.setOwner(owner);
        conversation.setInterested(interested);
        conversation.setCreatedAt(NOW.minusDays(1));
        conversation.setLastMessageAt(NOW.minusDays(1));

        OrganizationProfile shelter = new OrganizationProfile();
        shelter.setId(20L);
        shelter.setUser(owner);
        shelter.setOrganizationName("Refugio Patitas");
        PersonProfile ana = new PersonProfile();
        ana.setId(30L);
        ana.setUser(interested);
        ana.setFirstName("Ana");
        ana.setLastName("García");

        when(adoptionRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(luna));
        when(userRepository.getReferenceById(OWNER_ID)).thenReturn(owner);
        when(userRepository.getReferenceById(INTERESTED_ID)).thenReturn(interested);
        when(profileRepository.existsByUserId(anyLong())).thenReturn(true);
        when(profileRepository.findByUserIdIn(List.of(OWNER_ID))).thenReturn(List.of(shelter));
        when(profileRepository.findByUserIdIn(List.of(INTERESTED_ID))).thenReturn(List.of(ana));
        when(conversationRepository.findWithPublicationById(CONVERSATION_ID)).thenReturn(Optional.of(conversation));
        when(conversationRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            Conversation saved = invocation.getArgument(0);
            saved.setId(CONVERSATION_ID);
            return saved;
        });
        when(messageRepository.save(any())).thenAnswer(invocation -> {
            ChatMessage saved = invocation.getArgument(0);
            saved.setId(messageIds.incrementAndGet());
            when(messageRepository.findLastMessages(List.of(CONVERSATION_ID))).thenReturn(List.of(saved));
            return saved;
        });
        when(imageStorage.publicUrl(any())).thenAnswer(invocation -> "https://cdn/" + invocation.getArgument(0));
    }

    private static User user(long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private String bearer(long userId) {
        return "Bearer " + jwtUtil.generateToken(userId);
    }

    @Test
    void todasLasRutasRequierenToken() throws Exception {
        mockMvc.perform(get("/conversations")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
        mockMvc.perform(post("/conversations").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"publicationId\":1,\"body\":\"Hola\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/conversations/50")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/conversations/50/messages")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/conversations/50/messages").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"Hola\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/conversations/50/read")).andExpect(status().isUnauthorized());
        verifyNoInteractions(conversationRepository, messageRepository);
    }

    @Test
    void elPrimerMensajeCreaLaConversacionYResponde201() throws Exception {
        mockMvc.perform(post("/conversations").header("Authorization", bearer(INTERESTED_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"publicationId\":1,\"body\":\"Hola, ¿sigue en adopción?\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(CONVERSATION_ID))
                .andExpect(jsonPath("$.role").value("INTERESTED"))
                .andExpect(jsonPath("$.publication.id").value(1))
                .andExpect(jsonPath("$.publication.petName").value("Luna"))
                .andExpect(jsonPath("$.publication.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.publication.deleted").value(false))
                .andExpect(jsonPath("$.counterpart.displayName").value("Refugio Patitas"))
                .andExpect(jsonPath("$.counterpart.profileType").value("ORGANIZATION"))
                .andExpect(jsonPath("$.counterpart.email").doesNotExist())
                .andExpect(jsonPath("$.lastMessage.body").value("Hola, ¿sigue en adopción?"))
                .andExpect(jsonPath("$.lastMessage.senderRole").value("INTERESTED"))
                .andExpect(jsonPath("$.unreadCount").value(0))
                .andExpect(jsonPath("$.readOnly").value(false));
    }

    @Test
    void siLaConversacionYaExistiaResponde200() throws Exception {
        when(conversationRepository.findByPublicationIdAndInterestedId(1L, INTERESTED_ID))
                .thenReturn(Optional.of(conversation));

        mockMvc.perform(post("/conversations").header("Authorization", bearer(INTERESTED_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"publicationId\":1,\"body\":\"Hola de nuevo\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CONVERSATION_ID));
        verify(conversationRepository, never()).saveAndFlush(any());
    }

    @Test
    void elPrimerMensajeSeValida() throws Exception {
        String tooLong = "a".repeat(2001);
        for (String body : List.of("{\"publicationId\":1,\"body\":\"   \"}",
                "{\"publicationId\":1}",
                "{\"body\":\"Hola\"}",
                "{\"publicationId\":1,\"body\":\"" + tooLong + "\"}")) {
            mockMvc.perform(post("/conversations").header("Authorization", bearer(INTERESTED_ID))
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }
        verify(messageRepository, never()).save(any());
    }

    @Test
    void escribirSobreUnaPublicacionPropiaResponde403() throws Exception {
        mockMvc.perform(post("/conversations").header("Authorization", bearer(OWNER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"publicationId\":1,\"body\":\"Hola\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("CHAT_OWN_PUBLICATION"));
    }

    @Test
    void escribirSobreUnaAdoptadaResponde409() throws Exception {
        luna.setStatus(AdoptionStatus.ADOPTED);

        mockMvc.perform(post("/conversations").header("Authorization", bearer(INTERESTED_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"publicationId\":1,\"body\":\"Hola\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CHAT_READ_ONLY"));
    }

    @Test
    void laBandejaDelDuenoMuestraAlInteresadoYLosNoLeidos() throws Exception {
        ChatMessage last = new ChatMessage();
        last.setId(12L);
        last.setConversation(conversation);
        last.setSender(interested);
        last.setBody("¿Puedo ir el sábado?");
        last.setCreatedAt(NOW);
        when(conversationRepository.findForUser(eq(OWNER_ID), eq(true), eq(false), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of(conversation), PageRequest.of(0, 20), 1));
        when(messageRepository.findLastMessages(List.of(CONVERSATION_ID))).thenReturn(List.of(last));
        when(messageRepository.countUnread(List.of(CONVERSATION_ID), OWNER_ID))
                .thenReturn(List.of(new UnreadCount(CONVERSATION_ID, 2L)));

        mockMvc.perform(get("/conversations?role=OWNER").header("Authorization", bearer(OWNER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].role").value("OWNER"))
                .andExpect(jsonPath("$.items[0].counterpart.displayName").value("Ana García"))
                .andExpect(jsonPath("$.items[0].counterpart.profileId").value(30))
                .andExpect(jsonPath("$.items[0].lastMessage.body").value("¿Puedo ir el sábado?"))
                .andExpect(jsonPath("$.items[0].unreadCount").value(2))
                .andExpect(jsonPath("$.totalItems").value(1));
    }

    @Test
    void unRolInvalidoResponde400() throws Exception {
        mockMvc.perform(get("/conversations?role=ADMIN").header("Authorization", bearer(OWNER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void unaPersonaAjenaRecibe404() throws Exception {
        mockMvc.perform(get("/conversations/50").header("Authorization", bearer(STRANGER_ID)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CHAT_CONVERSATION_NOT_FOUND"));
        mockMvc.perform(get("/conversations/50/messages").header("Authorization", bearer(STRANGER_ID)))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/conversations/50/messages").header("Authorization", bearer(STRANGER_ID))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"body\":\"Hola\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/conversations/50/read").header("Authorization", bearer(STRANGER_ID)))
                .andExpect(status().isNotFound());
        verify(messageRepository, never()).save(any());
    }

    @Test
    void enviarResponde201ConElMensaje() throws Exception {
        mockMvc.perform(post("/conversations/50/messages").header("Authorization", bearer(OWNER_ID))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"body\":\"¡Sí, el sábado!\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.conversationId").value(CONVERSATION_ID))
                .andExpect(jsonPath("$.senderRole").value("OWNER"))
                .andExpect(jsonPath("$.body").value("¡Sí, el sábado!"));
    }

    @Test
    void enviarConLaPublicacionAdoptadaResponde409() throws Exception {
        luna.setStatus(AdoptionStatus.ADOPTED);

        mockMvc.perform(post("/conversations/50/messages").header("Authorization", bearer(OWNER_ID))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"body\":\"Hola\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CHAT_READ_ONLY"));
    }

    @Test
    void elHistorialSePaginaConCursor() throws Exception {
        ChatMessage message = new ChatMessage();
        message.setId(11L);
        message.setConversation(conversation);
        message.setSender(owner);
        message.setBody("Hola");
        message.setCreatedAt(NOW);
        when(messageRepository.findByConversationIdAndIdLessThan(eq(CONVERSATION_ID), eq(12L), any(Pageable.class)))
                .thenReturn(new SliceImpl<>(List.of(message), Pageable.ofSize(1), true));

        mockMvc.perform(get("/conversations/50/messages?before=12&pageSize=1")
                        .header("Authorization", bearer(INTERESTED_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(11))
                .andExpect(jsonPath("$.items[0].senderRole").value("OWNER"))
                .andExpect(jsonPath("$.hasMore").value(true));
    }

    @Test
    void marcarLeidoResponde204() throws Exception {
        when(messageRepository.findLastMessageId(CONVERSATION_ID)).thenReturn(Optional.of(12L));

        mockMvc.perform(put("/conversations/50/read").header("Authorization", bearer(INTERESTED_ID)))
                .andExpect(status().isNoContent());

        verify(conversationRepository).save(conversation);
    }
}
