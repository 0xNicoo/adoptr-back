package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.exception.custom.BadRequestException;
import com.example.adoptr_backend.exception.error.Error;
import com.example.adoptr_backend.model.*;
import com.example.adoptr_backend.repository.ChatRepository;
import com.example.adoptr_backend.repository.MessageRepository;
import com.example.adoptr_backend.repository.ProfileRepository;
import com.example.adoptr_backend.repository.PublicationRepository;
import com.example.adoptr_backend.service.ChatService;
import com.example.adoptr_backend.service.ImageService;
import com.example.adoptr_backend.service.PublicationService;
import com.example.adoptr_backend.service.dto.response.ChatDTO;
import com.example.adoptr_backend.service.dto.response.ProfileDTO;
import com.example.adoptr_backend.service.dto.response.PublicationChatDTO;
import com.example.adoptr_backend.service.dto.response.UserChatDTO;
import com.example.adoptr_backend.service.mapper.ChatMapper;
import com.example.adoptr_backend.service.mapper.ProfileMapper;
import com.example.adoptr_backend.util.AuthSupport;
import com.example.adoptr_backend.util.ChatMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final PublicationService publicationService;

    private final ChatRepository chatRepository;

    private final PublicationRepository publicationRepository;

    private final ProfileRepository profileRepository;

    private final MessageRepository messageRepository;

    private final ImageService imageService;

    @Override
    public ChatDTO get(Long id) {
        Long userId = AuthSupport.getUserId();
        Chat chat = getChat(id);
        if(!Objects.equals(chat.getAdopterUserId(), userId) && !Objects.equals(chat.getPublicationUserId(), userId)){
            throw new BadRequestException(Error.USER_NOT_IN_CHAT);
        }
        //TODO: esto obtenerlo desde el Usuario (agregarle el perfil al usuario)
        ChatDTO chatDTO = ChatMapper.MAPPER.toDto(chat);
        return setProfiles(chatDTO);
    }

    @Override
    public List<ChatDTO> getPublicationChats(Long publicationId) {
        Publication publication = publicationService.get(publicationId);
        Long userId = AuthSupport.getUserId();
        List<Chat> chats = chatRepository.findByPublicationIdAndUserId(publication.getId(), userId);
        List<ChatDTO> chatDTOList = new ArrayList<>();
        //Esto se hace para evitar que el creador de la publicacion cree un chat consigo mismo
        if(chats.isEmpty() && publication.getUser().getId().equals(userId)){
            throw new BadRequestException(Error.CHAT_NOT_FOUND_FOR_PUBLICATION);
        }
        //creo el chat si no tiene.
        if(chats.isEmpty()){
            Chat chat = createChat(publication, userId);
            ChatDTO chatDTO = ChatMapper.MAPPER.toDto(chat); //TODO: esto obtenerlo desde el Usuario (agregarle el perfil al usuario)
            chatDTOList.add(chatDTO);
        }else {
            chatDTOList = ChatMapper.MAPPER.toDto(chats);
        }
        chatDTOList.forEach(this::setProfiles);
        return chatDTOList;
    }

    @Override
    public List<PublicationChatDTO> getAllGroupByPublication() {
        Long userId = AuthSupport.getUserId();
        List<Chat> chats = chatRepository.findByAdopterUserIdOrPublicationUserId(userId, userId);
        List<Publication> publications = new ArrayList<>(chats.stream()
                .map(Chat::getPublication)
                .distinct()
                .toList());
        orderByLatestMessage(publications);
        List<PublicationChatDTO> publicationChatDTOList =  publications.stream().map(ChatMapper.MAPPER::toPublicationChatDTO).toList();
        publicationChatDTOList.forEach(pcDTO -> pcDTO.setS3Url(getS3Url(pcDTO)));
        return publicationChatDTOList;
    }

    @Override
    public List<UserChatDTO> getAllGroupByUsers() {
        Long userId = AuthSupport.getUserId();
        List<Chat> chats = chatRepository.findByAdopterUserIdOrPublicationUserId(userId, userId);

        Map<Profile, Chat> lastChatsWithProfiles = chats.stream()
                .collect(Collectors.toMap(
                        chat -> getProfileOfInterlocutor(chat, userId),
                        chat -> getLastChat(List.of(chat)),
                        (chat1, chat2) -> getLastChat(List.of(chat1, chat2))));

        return lastChatsWithProfiles.entrySet().stream()
                .sorted(Comparator.comparing(entry -> getLastMessageTimestamp(entry.getValue()), Comparator.reverseOrder()))
                .map(entry -> mapToUserChatDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatDTO> getAll() {
        Long userId = AuthSupport.getUserId();
        List<Chat> chats = chatRepository.findByAdopterUserIdOrPublicationUserId(userId, userId);
        List<ChatDTO> chatDTOList = ChatMapper.MAPPER.toDto(chats);
        //TODO: esto obtenerlo desde el Usuario (agregarle el perfil al usuario)
        chatDTOList.forEach(this::setProfiles);
        return chatDTOList;
    }

    @Override
    public void saveMassage(ChatMessage payload) {
        Chat chat = getChat(Long.valueOf(payload.getChatId()));
        Message message = new Message();
        message.setChat(chat);
        message.setContent(payload.getContent());
        message.setUserSenderId(Long.valueOf(payload.getUserSenderId()));
        messageRepository.save(message);
    }

    @Transactional
    private Chat createChat(Publication publication, Long adopterUserId) {
        Chat chat = new Chat();
        chat.setPublication(publication);
        chat.setPublicationUserId(publication.getUser().getId());
        chat.setAdopterUserId(adopterUserId);
        chat = chatRepository.save(chat);
        publication.addChat(chat);
        publicationRepository.save(publication);
        return chat;
    }

    //TODO: esto obtenerlo desde el Usuario (agregarle el perfil al usuario)
    private ChatDTO setProfiles(ChatDTO chatDTO){
        Profile interlocutorUserProfile = getProfile(chatDTO.getAdopterUserId());
        Profile publisherProfile = getProfile(chatDTO.getPublicationUserId());
        chatDTO.setInterlocutorUserProfile(mapToProfileDTO(interlocutorUserProfile));
        chatDTO.setPublicationUserProfile(mapToProfileDTO(publisherProfile));
        return chatDTO;
    }

    private Profile getProfile(Long userId){
        Optional<Profile> profileOptional = profileRepository.findByUserId(userId);
        if(profileOptional.isEmpty()){
            throw new BadRequestException(Error.PROFILE_NOT_FOUND);
        }
        return profileOptional.get();
    }

    private Chat getChat(Long id) {
        Optional<Chat> chatOptional = chatRepository.findById(id);
        if(chatOptional.isEmpty()){
            throw new BadRequestException(Error.CHAT_NOT_FOUND);
        }
        return chatOptional.get();
    }

    private void orderByLatestMessage(List<Publication> publications){
        publications.sort((pub1, pub2) -> {
            LocalDateTime pub1LatestMessage = pub1.getChats().stream()
                    .flatMap(chat -> chat.getMessages().stream())
                    .map(Message::getCreatedAt)
                    .max(LocalDateTime::compareTo)
                    .orElse(LocalDateTime.MIN);

            LocalDateTime pub2LatestMessage = pub2.getChats().stream()
                    .flatMap(chat -> chat.getMessages().stream())
                    .map(Message::getCreatedAt)
                    .max(LocalDateTime::compareTo)
                    .orElse(LocalDateTime.MIN);

            return pub2LatestMessage.compareTo(pub1LatestMessage);
        });
    }

    private String getS3Url(PublicationChatDTO publicationChatDTO){
        return imageService.getS3url(publicationChatDTO.getId(), ImageType.valueOf(publicationChatDTO.getType().name()));
    }

    private Profile getProfileOfInterlocutor(Chat chat, Long userId) {
        Long interlocutorId = getInterlocutor(chat, userId);
        return profileRepository.findByUserId(interlocutorId)
                .orElseThrow(() -> new BadRequestException(Error.PROFILE_NOT_FOUND));
    }

    private Long getInterlocutor(Chat chat, Long userId) {
        if (chat.getAdopterUserId().equals(userId)) {
            return chat.getPublicationUserId();
        }
        return chat.getAdopterUserId();
    }

    private Chat getLastChat(List<Chat> chats) {
        return chats.stream()
                .max(Comparator.comparing(this::getLastMessageTimestamp))
                .orElse(null);
    }

    private LocalDateTime getLastMessageTimestamp(Chat chat) {
        return chat.getMessages().stream()
                .map(Message::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.MIN);
    }
    private UserChatDTO mapToUserChatDTO(Profile profile, Chat chat) {
        ProfileDTO profileDTO = mapToProfileDTO(profile);
        return new UserChatDTO(profileDTO, chat.getId());
    }

    private ProfileDTO mapToProfileDTO(Profile profile) {
        ProfileDTO profileDTO = ProfileMapper.MAPPER.toDto(profile);
        String s3Url = imageService.getS3url(profile.getId(), ImageType.PROFILE);
        profileDTO.setS3Url(s3Url);
        return profileDTO;
    }
}
