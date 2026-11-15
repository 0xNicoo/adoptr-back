package com.adoptr.adoptr.chat.service;

import com.adoptr.adoptr.adoption.model.AdoptionPublication;
import com.adoptr.adoptr.adoption.model.AdoptionStatus;
import com.adoptr.adoptr.adoption.repository.AdoptionPublicationRepository;
import com.adoptr.adoptr.chat.dto.request.MessageDTOin;
import com.adoptr.adoptr.chat.dto.request.StartConversationDTOin;
import com.adoptr.adoptr.chat.dto.response.ConversationDTO;
import com.adoptr.adoptr.chat.dto.response.MessageDTO;
import com.adoptr.adoptr.chat.dto.response.MessagePageDTO;
import com.adoptr.adoptr.chat.event.MessageSentEvent;
import com.adoptr.adoptr.chat.model.ChatMessage;
import com.adoptr.adoptr.chat.model.ChatRole;
import com.adoptr.adoptr.chat.model.Conversation;
import com.adoptr.adoptr.chat.repository.ChatMessageRepository;
import com.adoptr.adoptr.chat.repository.ConversationRepository;
import com.adoptr.adoptr.chat.service.impl.ConversationAssembler;
import com.adoptr.adoptr.chat.service.impl.ConversationServiceImpl;
import com.adoptr.adoptr.profile.repository.ProfileRepository;
import com.adoptr.adoptr.shared.config.ClockConfig;
import com.adoptr.adoptr.shared.exception.custom.BadRequestException;
import com.adoptr.adoptr.shared.exception.custom.ConflictException;
import com.adoptr.adoptr.shared.exception.custom.ForbiddenException;
import com.adoptr.adoptr.shared.exception.custom.NotFoundException;
import com.adoptr.adoptr.user.model.User;
import com.adoptr.adoptr.user.repository.UserRepository;
import com.adoptr.adoptr.shared.util.MembershipSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ConversationServiceImplTest {

    private static final long OWNER_ID = 7L;
    private static final long INTERESTED_ID = 8L;
    private static final long STRANGER_ID = 9L;
    private static final long PUBLICATION_ID = 1L;
    private static final long CONVERSATION_ID = 50L;
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-15T15:00:00Z"), ClockConfig.ZONE);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 15, 12, 0);

    private MembershipSupport membershipSupport;
    private ConversationRepository conversationRepository;
    private ChatMessageRepository messageRepository;
    private AdoptionPublicationRepository adoptionRepository;
    private ProfileRepository profileRepository;
    private ConversationAssembler assembler;
    private ApplicationEventPublisher events;
    private ConversationService service;

    private User owner;
    private User interested;
    private AdoptionPublication publication;
    private final AtomicLong messageIds = new AtomicLong(100);

    @BeforeEach
    void setUp() {
        membershipSupport = mock(MembershipSupport.class);
        conversationRepository = mock(ConversationRepository.class);
        messageRepository = mock(ChatMessageRepository.class);
        adoptionRepository = mock(AdoptionPublicationRepository.class);
        profileRepository = mock(ProfileRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        assembler = mock(ConversationAssembler.class);
        events = mock(ApplicationEventPublisher.class);
        service = new ConversationServiceImpl(membershipSupport, conversationRepository, messageRepository,
                adoptionRepository, profileRepository, userRepository, assembler, events, CLOCK);

        owner = user(OWNER_ID);
        interested = user(INTERESTED_ID);
        publication = new AdoptionPublication();
        publication.setId(PUBLICATION_ID);
        publication.setAuthor(owner);
        publication.setStatus(AdoptionStatus.AVAILABLE);

        when(userRepository.getReferenceById(OWNER_ID)).thenReturn(owner);
        when(userRepository.getReferenceById(INTERESTED_ID)).thenReturn(interested);
        when(adoptionRepository.findByIdAndDeletedAtIsNull(PUBLICATION_ID)).thenReturn(Optional.of(publication));
        when(profileRepository.existsByUserId(anyLong())).thenReturn(true);
        when(conversationRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            Conversation saved = invocation.getArgument(0);
            saved.setId(CONVERSATION_ID);
            return saved;
        });
        when(messageRepository.save(any())).thenAnswer(invocation -> {
            ChatMessage saved = invocation.getArgument(0);
            saved.setId(messageIds.incrementAndGet());
            return saved;
        });
        when(assembler.toDto(any(), anyLong())).thenReturn(mock(ConversationDTO.class));
    }

    private static User user(long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private void loggedAs(long userId) {
        when(membershipSupport.getUserId()).thenReturn(userId);
    }

    private static StartConversationDTOin start(String body) {
        StartConversationDTOin request = new StartConversationDTOin();
        request.setPublicationId(PUBLICATION_ID);
        request.setBody(body);
        return request;
    }

    private static MessageDTOin message(String body) {
        MessageDTOin request = new MessageDTOin();
        request.setBody(body);
        return request;
    }

    private Conversation existingConversation() {
        Conversation conversation = new Conversation();
        conversation.setId(CONVERSATION_ID);
        conversation.setPublication(publication);
        conversation.setOwner(owner);
        conversation.setInterested(interested);
        conversation.setCreatedAt(NOW.minusDays(1));
        conversation.setLastMessageAt(NOW.minusDays(1));
        when(conversationRepository.findWithPublicationById(CONVERSATION_ID)).thenReturn(Optional.of(conversation));
        when(conversationRepository.findByPublicationIdAndInterestedId(PUBLICATION_ID, INTERESTED_ID))
                .thenReturn(Optional.of(conversation));
        return conversation;
    }

    // --- start ---

    @Test
    void elPrimerMensajeCreaLaConversacionConDuenoEInteresado() {
        loggedAs(INTERESTED_ID);

        StartedConversation result = service.start(start("  Hola, ¿sigue en adopción?  "));

        assertThat(result.created()).isTrue();
        ArgumentCaptor<Conversation> conversation = ArgumentCaptor.forClass(Conversation.class);
        verify(conversationRepository).saveAndFlush(conversation.capture());
        assertThat(conversation.getValue().getPublication()).isSameAs(publication);
        assertThat(conversation.getValue().getOwner()).isSameAs(owner);
        assertThat(conversation.getValue().getInterested()).isSameAs(interested);
        assertThat(conversation.getValue().getCreatedAt()).isEqualTo(NOW);

        ArgumentCaptor<ChatMessage> message = ArgumentCaptor.forClass(ChatMessage.class);
        verify(messageRepository).save(message.capture());
        assertThat(message.getValue().getBody()).isEqualTo("Hola, ¿sigue en adopción?");
        assertThat(message.getValue().getSender()).isSameAs(interested);
        assertThat(message.getValue().getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void siYaHabiaConversacionSumaElMensajeSinCrearOtra() {
        loggedAs(INTERESTED_ID);
        Conversation conversation = existingConversation();

        StartedConversation result = service.start(start("Hola de nuevo"));

        assertThat(result.created()).isFalse();
        verify(conversationRepository, never()).saveAndFlush(any());
        assertThat(conversation.getLastMessageAt()).isEqualTo(NOW);
    }

    @Test
    void noSePuedeEscribirSobreUnaPublicacionPropia() {
        loggedAs(OWNER_ID);

        assertThatThrownBy(() -> service.start(start("Hola")))
                .isInstanceOf(ForbiddenException.class)
                .extracting("code").isEqualTo("CHAT_OWN_PUBLICATION");
        verifyNoInteractions(messageRepository);
    }

    @Test
    void escribirExigePerfil() {
        loggedAs(INTERESTED_ID);
        when(profileRepository.existsByUserId(INTERESTED_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.start(start("Hola")))
                .isInstanceOf(ForbiddenException.class)
                .extracting("code").isEqualTo("PROFILE_REQUIRED");
        verifyNoInteractions(messageRepository);
    }

    @Test
    void unaPublicacionInexistenteODadaDeBajaResponde404() {
        loggedAs(INTERESTED_ID);
        when(adoptionRepository.findByIdAndDeletedAtIsNull(PUBLICATION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.start(start("Hola")))
                .isInstanceOf(NotFoundException.class)
                .extracting("code").isEqualTo("PUBLICATION_NOT_FOUND");
    }

    @Test
    void noSePuedeIniciarUnaConversacionSobreUnaAdoptada() {
        loggedAs(INTERESTED_ID);
        publication.setStatus(AdoptionStatus.ADOPTED);

        assertThatThrownBy(() -> service.start(start("Hola")))
                .isInstanceOf(ConflictException.class)
                .extracting("code").isEqualTo("CHAT_READ_ONLY");
        verifyNoInteractions(messageRepository);
    }

    @Test
    void dosPrimerosMensajesSimultaneosResponden409() {
        loggedAs(INTERESTED_ID);
        doThrow(new DataIntegrityViolationException("dup")).when(conversationRepository).saveAndFlush(any());

        assertThatThrownBy(() -> service.start(start("Hola")))
                .isInstanceOf(ConflictException.class)
                .extracting("code").isEqualTo("CHAT_CONFLICT");
    }

    // --- send ---

    @Test
    void enviarGuardaElMensajeYAvisaALosDos() {
        loggedAs(OWNER_ID);
        existingConversation();

        MessageDTO sent = service.send(CONVERSATION_ID, message("¡Sí! ¿Cuándo podés venir?"));

        assertThat(sent.senderRole()).isEqualTo(ChatRole.OWNER);
        assertThat(sent.conversationId()).isEqualTo(CONVERSATION_ID);
        assertThat(sent.body()).isEqualTo("¡Sí! ¿Cuándo podés venir?");
        ArgumentCaptor<MessageSentEvent> event = ArgumentCaptor.forClass(MessageSentEvent.class);
        verify(events).publishEvent(event.capture());
        assertThat(event.getValue().recipientUserIds()).containsExactly(OWNER_ID, INTERESTED_ID);
        assertThat(event.getValue().message()).isEqualTo(sent);
    }

    @Test
    void quienEscribeQuedaConTodoLeido() {
        loggedAs(INTERESTED_ID);
        Conversation conversation = existingConversation();
        conversation.setInterestedLastReadMessageId(3L);

        MessageDTO sent = service.send(CONVERSATION_ID, message("Hola"));

        assertThat(conversation.getInterestedLastReadMessageId()).isEqualTo(sent.id());
        assertThat(conversation.getOwnerLastReadMessageId()).isNull();
    }

    @Test
    void conLaPublicacionAdoptadaElChatEsDeSoloLectura() {
        loggedAs(INTERESTED_ID);
        existingConversation();
        publication.setStatus(AdoptionStatus.ADOPTED);

        assertThatThrownBy(() -> service.send(CONVERSATION_ID, message("Hola")))
                .isInstanceOf(ConflictException.class)
                .extracting("code").isEqualTo("CHAT_READ_ONLY");
        verify(messageRepository, never()).save(any());
        verifyNoInteractions(events);
    }

    @Test
    void conLaPublicacionDadaDeBajaElChatEsDeSoloLectura() {
        loggedAs(OWNER_ID);
        existingConversation();
        publication.setDeletedAt(NOW.minusHours(1));

        assertThatThrownBy(() -> service.send(CONVERSATION_ID, message("Hola")))
                .isInstanceOf(ConflictException.class)
                .extracting("code").isEqualTo("CHAT_READ_ONLY");
    }

    @Test
    void siVuelveAEstarEnAdopcionElChatSeReabre() {
        loggedAs(INTERESTED_ID);
        existingConversation();
        publication.setStatus(AdoptionStatus.ADOPTED);
        publication.setStatus(AdoptionStatus.AVAILABLE);

        assertThat(service.send(CONVERSATION_ID, message("¿Sigue disponible?")).body())
                .isEqualTo("¿Sigue disponible?");
    }

    @Test
    void unaPersonaAjenaNoPuedeVerNiEscribir() {
        loggedAs(STRANGER_ID);
        existingConversation();

        assertThatThrownBy(() -> service.send(CONVERSATION_ID, message("Hola")))
                .isInstanceOf(NotFoundException.class)
                .extracting("code").isEqualTo("CHAT_CONVERSATION_NOT_FOUND");
        assertThatThrownBy(() -> service.get(CONVERSATION_ID))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.messages(CONVERSATION_ID, null, 30))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.markRead(CONVERSATION_ID))
                .isInstanceOf(NotFoundException.class);
        verify(messageRepository, never()).save(any());
    }

    // --- list / messages / read ---

    @Test
    void elFiltroPorRolEligeQueBandejaConsultar() {
        loggedAs(OWNER_ID);
        when(conversationRepository.findForUser(anyLong(), anyBoolean(), anyBoolean(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        service.list(ChatRole.OWNER, null, 0, 20);
        verify(conversationRepository).findForUser(eq(OWNER_ID), eq(true), eq(false), isNull(), any());

        service.list(ChatRole.INTERESTED, PUBLICATION_ID, 0, 20);
        verify(conversationRepository).findForUser(eq(OWNER_ID), eq(false), eq(true), eq(PUBLICATION_ID), any());

        service.list(null, null, 0, 20);
        verify(conversationRepository).findForUser(eq(OWNER_ID), eq(true), eq(true), isNull(), any());
    }

    @Test
    void elHistorialVieneDelMasViejoAlMasNuevo() {
        loggedAs(INTERESTED_ID);
        Conversation conversation = existingConversation();
        ChatMessage newer = chatMessage(conversation, 12L, owner);
        ChatMessage older = chatMessage(conversation, 11L, interested);
        when(messageRepository.findByConversationIdAndIdLessThan(eq(CONVERSATION_ID), eq(13L), any(Pageable.class)))
                .thenReturn(new SliceImpl<>(List.of(newer, older), Pageable.ofSize(2), true));

        MessagePageDTO page = service.messages(CONVERSATION_ID, 13L, 2);

        assertThat(page.items()).extracting(MessageDTO::id).containsExactly(11L, 12L);
        assertThat(page.items()).extracting(MessageDTO::senderRole)
                .containsExactly(ChatRole.INTERESTED, ChatRole.OWNER);
        assertThat(page.hasMore()).isTrue();
    }

    @Test
    void elTamanoDelHistorialSeValida() {
        loggedAs(INTERESTED_ID);
        existingConversation();

        assertThatThrownBy(() -> service.messages(CONVERSATION_ID, null, 0))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.messages(CONVERSATION_ID, null, 51))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void marcarLeidoAvanzaSoloLaMarcaDeQuienConsulta() {
        loggedAs(OWNER_ID);
        Conversation conversation = existingConversation();
        conversation.setInterestedLastReadMessageId(20L);
        when(messageRepository.findLastMessageId(CONVERSATION_ID)).thenReturn(Optional.of(20L));

        service.markRead(CONVERSATION_ID);

        assertThat(conversation.getOwnerLastReadMessageId()).isEqualTo(20L);
        assertThat(conversation.getInterestedLastReadMessageId()).isEqualTo(20L);
    }

    @Test
    void laMarcaDeLecturaNoRetrocede() {
        Conversation conversation = existingConversation();
        conversation.markRead(ChatRole.OWNER, 30L);
        conversation.markRead(ChatRole.OWNER, 25L);

        assertThat(conversation.getOwnerLastReadMessageId()).isEqualTo(30L);
    }

    private static ChatMessage chatMessage(Conversation conversation, long id, User sender) {
        ChatMessage message = new ChatMessage();
        message.setId(id);
        message.setConversation(conversation);
        message.setSender(sender);
        message.setBody("mensaje " + id);
        message.setCreatedAt(NOW);
        return message;
    }
}
