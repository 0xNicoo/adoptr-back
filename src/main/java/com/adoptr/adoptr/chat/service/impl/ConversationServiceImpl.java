package com.adoptr.adoptr.chat.service.impl;

import com.adoptr.adoptr.adoption.model.AdoptionPublication;
import com.adoptr.adoptr.adoption.repository.AdoptionPublicationRepository;
import com.adoptr.adoptr.chat.dto.request.MessageDTOin;
import com.adoptr.adoptr.chat.dto.request.StartConversationDTOin;
import com.adoptr.adoptr.chat.dto.response.ConversationDTO;
import com.adoptr.adoptr.chat.dto.response.MessageDTO;
import com.adoptr.adoptr.chat.dto.response.MessagePageDTO;
import com.adoptr.adoptr.chat.error.ChatError;
import com.adoptr.adoptr.chat.event.MessageSentEvent;
import com.adoptr.adoptr.chat.model.ChatMessage;
import com.adoptr.adoptr.chat.model.ChatRole;
import com.adoptr.adoptr.chat.model.Conversation;
import com.adoptr.adoptr.chat.repository.ChatMessageRepository;
import com.adoptr.adoptr.chat.repository.ConversationRepository;
import com.adoptr.adoptr.chat.service.ConversationService;
import com.adoptr.adoptr.chat.service.StartedConversation;
import com.adoptr.adoptr.profile.error.ProfileError;
import com.adoptr.adoptr.profile.repository.ProfileRepository;
import com.adoptr.adoptr.publication.error.PublicationError;
import com.adoptr.adoptr.shared.dto.PageDTO;
import com.adoptr.adoptr.shared.exception.custom.ConflictException;
import com.adoptr.adoptr.shared.exception.custom.ForbiddenException;
import com.adoptr.adoptr.shared.exception.custom.NotFoundException;
import com.adoptr.adoptr.shared.util.MembershipSupport;
import com.adoptr.adoptr.shared.util.Paging;
import com.adoptr.adoptr.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ConversationServiceImpl implements ConversationService {

    static final Sort NEWEST_MESSAGES_FIRST = Sort.by(Sort.Order.desc("id"));

    private final MembershipSupport membershipSupport;
    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final AdoptionPublicationRepository adoptionRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ConversationAssembler assembler;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public ConversationServiceImpl(MembershipSupport membershipSupport,
                                   ConversationRepository conversationRepository,
                                   ChatMessageRepository messageRepository,
                                   AdoptionPublicationRepository adoptionRepository,
                                   ProfileRepository profileRepository,
                                   UserRepository userRepository,
                                   ConversationAssembler assembler,
                                   ApplicationEventPublisher events,
                                   Clock clock) {
        this.membershipSupport = membershipSupport;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.adoptionRepository = adoptionRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.assembler = assembler;
        this.events = events;
        this.clock = clock;
    }

    /**
     * Orden de los controles: 404 (no existe o fue dada de baja), 403 (es propia), 403 (sin perfil),
     * 409 (ya no está en adopción).
     */
    @Override
    @Transactional
    public StartedConversation start(StartConversationDTOin request) {
        Long userId = membershipSupport.getUserId();
        AdoptionPublication publication = adoptionRepository.findByIdAndDeletedAtIsNull(request.getPublicationId())
                .orElseThrow(() -> new NotFoundException(PublicationError.NOT_FOUND));
        if (userId.equals(publication.getAuthor().getId())) {
            throw new ForbiddenException(ChatError.OWN_PUBLICATION);
        }
        if (!profileRepository.existsByUserId(userId)) {
            throw new ForbiddenException(ProfileError.REQUIRED);
        }
        if (ChatRules.isReadOnly(publication)) {
            throw new ConflictException(ChatError.READ_ONLY);
        }

        LocalDateTime now = LocalDateTime.now(clock);
        Optional<Conversation> existing =
                conversationRepository.findByPublicationIdAndInterestedId(publication.getId(), userId);
        Conversation conversation = existing.orElseGet(() -> create(publication, userId, now));
        append(conversation, userId, request.getBody(), now);
        return new StartedConversation(assembler.toDto(conversation, userId), existing.isEmpty());
    }

    private Conversation create(AdoptionPublication publication, Long interestedId, LocalDateTime now) {
        Conversation conversation = new Conversation();
        conversation.setPublication(publication);
        conversation.setOwner(publication.getAuthor());
        conversation.setInterested(userRepository.getReferenceById(interestedId));
        conversation.setCreatedAt(now);
        conversation.setLastMessageAt(now);
        try {
            return conversationRepository.saveAndFlush(conversation);
        } catch (DataIntegrityViolationException e) {
            // Dos primeros mensajes simultáneos de la misma persona: la restricción única frena a uno.
            throw new ConflictException(ChatError.CONFLICT);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageDTO<ConversationDTO> list(ChatRole role, Long publicationId, int page, int pageSize) {
        Long userId = membershipSupport.getUserId();
        Pageable pageable = Paging.pageable(page, pageSize, Sort.unsorted());
        boolean asOwner = role == null || role == ChatRole.OWNER;
        boolean asInterested = role == null || role == ChatRole.INTERESTED;
        return assembler.page(
                conversationRepository.findForUser(userId, asOwner, asInterested, publicationId, pageable), userId);
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationDTO get(Long conversationId) {
        Long userId = membershipSupport.getUserId();
        return assembler.toDto(findParticipating(conversationId, userId), userId);
    }

    @Override
    @Transactional(readOnly = true)
    public MessagePageDTO messages(Long conversationId, Long before, int pageSize) {
        Conversation conversation = findParticipating(conversationId, membershipSupport.getUserId());
        Pageable pageable = Paging.pageable(0, pageSize, NEWEST_MESSAGES_FIRST);
        Slice<ChatMessage> slice = before == null
                ? messageRepository.findByConversationId(conversationId, pageable)
                : messageRepository.findByConversationIdAndIdLessThan(conversationId, before, pageable);
        List<MessageDTO> items = slice.getContent().reversed().stream()
                .map(message -> MessageDTO.of(message, conversation))
                .toList();
        return new MessagePageDTO(items, slice.hasNext());
    }

    @Override
    @Transactional
    public MessageDTO send(Long conversationId, MessageDTOin request) {
        Long userId = membershipSupport.getUserId();
        Conversation conversation = findParticipating(conversationId, userId);
        if (ChatRules.isReadOnly(conversation.getPublication())) {
            throw new ConflictException(ChatError.READ_ONLY);
        }
        return append(conversation, userId, request.getBody(), LocalDateTime.now(clock));
    }

    @Override
    @Transactional
    public void markRead(Long conversationId) {
        Long userId = membershipSupport.getUserId();
        Conversation conversation = findParticipating(conversationId, userId);
        messageRepository.findLastMessageId(conversationId).ifPresent(lastId -> {
            conversation.markRead(conversation.roleOf(userId), lastId);
            conversationRepository.save(conversation);
        });
    }

    /** 404 tanto si no existe como si es de otras personas: no revela qué conversaciones existen. */
    private Conversation findParticipating(Long conversationId, Long userId) {
        return conversationRepository.findWithPublicationById(conversationId)
                .filter(conversation -> conversation.roleOf(userId) != null)
                .orElseThrow(() -> new NotFoundException(ChatError.CONVERSATION_NOT_FOUND));
    }

    /** Guarda el mensaje, actualiza la actividad y avisa a los dos participantes cuando confirme la transacción. */
    private MessageDTO append(Conversation conversation, Long senderId, String body, LocalDateTime now) {
        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSender(userRepository.getReferenceById(senderId));
        message.setBody(body.trim());
        message.setCreatedAt(now);
        message = messageRepository.save(message);

        conversation.setLastMessageAt(now);
        // Quien escribe ya vio todo lo anterior.
        conversation.markRead(conversation.roleOf(senderId), message.getId());
        conversationRepository.save(conversation);

        MessageDTO dto = MessageDTO.of(message, conversation);
        events.publishEvent(new MessageSentEvent(
                List.of(conversation.getOwner().getId(), conversation.getInterested().getId()), dto));
        return dto;
    }
}
