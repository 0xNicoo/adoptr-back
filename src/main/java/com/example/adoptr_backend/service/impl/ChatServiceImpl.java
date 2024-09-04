package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.exception.custom.BadRequestException;
import com.example.adoptr_backend.exception.error.Error;
import com.example.adoptr_backend.model.Chat;
import com.example.adoptr_backend.model.Message;
import com.example.adoptr_backend.model.Profile;
import com.example.adoptr_backend.model.Publication;
import com.example.adoptr_backend.repository.ChatRepository;
import com.example.adoptr_backend.repository.MessageRepository;
import com.example.adoptr_backend.repository.ProfileRepository;
import com.example.adoptr_backend.repository.PublicationRepository;
import com.example.adoptr_backend.service.ChatService;
import com.example.adoptr_backend.service.PublicationService;
import com.example.adoptr_backend.service.dto.response.ChatDTO;
import com.example.adoptr_backend.service.mapper.ChatMapper;
import com.example.adoptr_backend.util.AuthSupport;
import com.example.adoptr_backend.util.ChatMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final PublicationService publicationService;

    private final ChatRepository chatRepository;

    private final PublicationRepository publicationRepository;

    private final ProfileRepository profileRepository;

    private final MessageRepository messageRepository;

    @Override
    public ChatDTO get(Long id) {
        Long userId = AuthSupport.getUserId();
        Chat chat = getChat(id);
        if(!Objects.equals(chat.getAdopterUserId(), userId) && !Objects.equals(chat.getPublicationUserId(), userId)){
            throw new BadRequestException(Error.USER_NOT_IN_CHAT);
        }
        //TODO: esto obtenerlo desde el Usuario (agregarle el perfil al usuario)
        ChatDTO chatDTO = ChatMapper.MAPPER.toDto(chat);
        return setUsersName(chatDTO);
    }

    @Transactional
    @Override
    public ChatDTO getByPublication(Long publicationId) {
        Publication publication = publicationService.get(publicationId);
        Long adopterUserId = AuthSupport.getUserId();
        // Se verifica por si se le pega con el toke del dueño de la publicacion, para evitar bugs.
        if(publication.getUser().getId().equals(adopterUserId)){
            throw new BadRequestException(Error.CHAT_ERROR);
        }
        Optional<Chat> chatOptional = chatRepository.findByPublicationIdAndAdopterUserId(publication.getId(), adopterUserId);
        if (chatOptional.isPresent()){
            ChatDTO chatDTO = ChatMapper.MAPPER.toDto(chatOptional.get());
            return setUsersName(chatDTO);
        }
        Chat chat = createChat(publication, adopterUserId);
        //TODO: esto obtenerlo desde el Usuario (agregarle el perfil al usuario)
        ChatDTO chatDTO = ChatMapper.MAPPER.toDto(chat);
        return setUsersName(chatDTO);
    }

    @Override
    public List<ChatDTO> getAll() {
        Long userId = AuthSupport.getUserId();
        List<Chat> chats = chatRepository.findByAdopterUserIdOrPublicationUserId(userId, userId);
        List<ChatDTO> chatDTOList = ChatMapper.MAPPER.toDto(chats);
        //TODO: esto obtenerlo desde el Usuario (agregarle el perfil al usuario)
        chatDTOList.forEach(this::setUsersName);
        return chatDTOList;
    }

    @Override
    public void saveMassage(ChatMessage payload) {
        Chat chat = getChat(Long.valueOf(payload.getChatId()));
        Message message = new Message();
        message.setChat(chat);
        message.setContent(payload.getContent());
        message.setUserId(Long.valueOf(payload.getSenderId()));
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
    private ChatDTO setUsersName(ChatDTO chatDTO){
        Profile adopterProfile = getProfile(chatDTO.getAdopterUserId());
        Profile publisherProfile = getProfile(chatDTO.getPublicationUserId());
        String adopterName = adopterProfile.getFirstName() + " " + adopterProfile.getLastName();
        String publisherName = publisherProfile.getFirstName() + " " + publisherProfile.getLastName();
        chatDTO.setAdopterUserName(adopterName);
        chatDTO.setPublicationUserName(publisherName);
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
}
