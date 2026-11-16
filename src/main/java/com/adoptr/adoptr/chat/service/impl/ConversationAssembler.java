package com.adoptr.adoptr.chat.service.impl;

import com.adoptr.adoptr.adoption.dto.response.OwnerDTO;
import com.adoptr.adoptr.adoption.mapper.AdoptionMapper;
import com.adoptr.adoptr.adoption.model.AdoptionPublication;
import com.adoptr.adoptr.chat.dto.response.ConversationDTO;
import com.adoptr.adoptr.chat.dto.response.ConversationPublicationDTO;
import com.adoptr.adoptr.chat.dto.response.MessageDTO;
import com.adoptr.adoptr.chat.model.ChatMessage;
import com.adoptr.adoptr.chat.model.ChatRole;
import com.adoptr.adoptr.chat.model.Conversation;
import com.adoptr.adoptr.chat.repository.ChatMessageRepository;
import com.adoptr.adoptr.chat.repository.UnreadCount;
import com.adoptr.adoptr.profile.model.Profile;
import com.adoptr.adoptr.profile.repository.ProfilePhotoRepository;
import com.adoptr.adoptr.profile.repository.ProfileRepository;
import com.adoptr.adoptr.publication.model.Publication;
import com.adoptr.adoptr.publication.repository.PublicationPhotoRepository;
import com.adoptr.adoptr.shared.dto.PageDTO;
import com.adoptr.adoptr.shared.storage.ImageStorage;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Arma las conversaciones vistas por un usuario. Resuelve toda la lista con una consulta por dato
 * (últimos mensajes, no leídos, perfiles, fotos de perfil y portadas), no una por conversación.
 * Las conversaciones tienen que traer la publicación cargada.
 */
@Component
public class ConversationAssembler {

    private final ChatMessageRepository messageRepository;
    private final ProfileRepository profileRepository;
    private final ProfilePhotoRepository profilePhotoRepository;
    private final PublicationPhotoRepository photoRepository;
    private final ImageStorage imageStorage;

    public ConversationAssembler(ChatMessageRepository messageRepository,
                                 ProfileRepository profileRepository,
                                 ProfilePhotoRepository profilePhotoRepository,
                                 PublicationPhotoRepository photoRepository,
                                 ImageStorage imageStorage) {
        this.messageRepository = messageRepository;
        this.profileRepository = profileRepository;
        this.profilePhotoRepository = profilePhotoRepository;
        this.photoRepository = photoRepository;
        this.imageStorage = imageStorage;
    }

    public PageDTO<ConversationDTO> page(Page<Conversation> page, Long viewerId) {
        return new PageDTO<>(toDtos(page.getContent(), viewerId), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    public ConversationDTO toDto(Conversation conversation, Long viewerId) {
        return toDtos(List.of(conversation), viewerId).getFirst();
    }

    public List<ConversationDTO> toDtos(List<Conversation> conversations, Long viewerId) {
        if (conversations.isEmpty()) {
            return List.of();
        }
        List<Long> ids = conversations.stream().map(Conversation::getId).toList();
        Map<Long, ChatMessage> lastMessages = messageRepository.findLastMessages(ids).stream()
                .collect(Collectors.toMap(message -> message.getConversation().getId(), Function.identity()));
        Map<Long, Long> unread = messageRepository.countUnread(ids, viewerId).stream()
                .collect(Collectors.toMap(UnreadCount::conversationId, UnreadCount::count));
        Map<Long, OwnerDTO> counterparts = counterparts(conversations, viewerId);
        Map<Long, String> covers = covers(conversations);

        return conversations.stream().map(conversation -> {
            ChatRole role = conversation.roleOf(viewerId);
            Publication publication = conversation.getPublication();
            ChatMessage last = lastMessages.get(conversation.getId());
            return new ConversationDTO(
                    conversation.getId(),
                    role,
                    publication(publication, covers.get(publication.getId())),
                    counterparts.get(counterpartId(conversation, role)),
                    last == null ? null : MessageDTO.of(last, conversation),
                    unread.getOrDefault(conversation.getId(), 0L),
                    ChatRules.isReadOnly(publication),
                    conversation.getCreatedAt(),
                    conversation.getLastMessageAt());
        }).toList();
    }

    private static Long counterpartId(Conversation conversation, ChatRole role) {
        return role == ChatRole.OWNER ? conversation.getInterested().getId() : conversation.getOwner().getId();
    }

    /** Datos públicos de la otra persona de cada conversación, por id de usuario. */
    private Map<Long, OwnerDTO> counterparts(List<Conversation> conversations, Long viewerId) {
        List<Long> userIds = conversations.stream()
                .map(conversation -> counterpartId(conversation, conversation.roleOf(viewerId)))
                .distinct()
                .toList();
        List<Profile> profiles = profileRepository.findByUserIdIn(userIds);
        Map<Long, String> photos = profilePhotos(profiles);
        return profiles.stream()
                .collect(Collectors.toMap(profile -> profile.getUser().getId(),
                        profile -> AdoptionMapper.toOwner(profile, photos.get(profile.getId())),
                        (first, other) -> first));
    }

    private Map<Long, String> profilePhotos(List<Profile> profiles) {
        if (profiles.isEmpty()) {
            return Map.of();
        }
        List<Long> profileIds = profiles.stream().map(Profile::getId).toList();
        return profilePhotoRepository.findByProfileIdIn(profileIds).stream()
                .collect(Collectors.toMap(photo -> photo.getProfile().getId(),
                        photo -> imageStorage.publicUrl(photo.getS3Key()),
                        (first, other) -> first));
    }

    private Map<Long, String> covers(List<Conversation> conversations) {
        List<Long> publicationIds = conversations.stream()
                .map(conversation -> conversation.getPublication().getId())
                .distinct()
                .toList();
        return photoRepository.findByPublicationIdInOrderBySortOrderAsc(publicationIds).stream()
                .collect(Collectors.toMap(photo -> photo.getPublication().getId(),
                        photo -> imageStorage.publicUrl(photo.getS3Key()),
                        (cover, other) -> cover));
    }

    private static ConversationPublicationDTO publication(Publication publication, String coverUrl) {
        boolean deleted = publication.getDeletedAt() != null;
        if (Hibernate.unproxy(publication) instanceof AdoptionPublication adoption) {
            return new ConversationPublicationDTO(adoption.getId(), adoption.getType(), adoption.getPetName(),
                    adoption.getSpecies(), adoption.getStatus(), coverUrl, deleted);
        }
        return new ConversationPublicationDTO(publication.getId(), publication.getType(), null, null, null,
                coverUrl, deleted);
    }
}
