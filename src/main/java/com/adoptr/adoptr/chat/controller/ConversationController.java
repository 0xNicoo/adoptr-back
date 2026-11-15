package com.adoptr.adoptr.chat.controller;

import com.adoptr.adoptr.chat.dto.request.MessageDTOin;
import com.adoptr.adoptr.chat.dto.request.StartConversationDTOin;
import com.adoptr.adoptr.chat.dto.response.ConversationDTO;
import com.adoptr.adoptr.chat.dto.response.MessageDTO;
import com.adoptr.adoptr.chat.dto.response.MessagePageDTO;
import com.adoptr.adoptr.chat.model.ChatRole;
import com.adoptr.adoptr.chat.service.ConversationService;
import com.adoptr.adoptr.chat.service.StartedConversation;
import com.adoptr.adoptr.shared.dto.PageDTO;
import com.adoptr.adoptr.shared.util.Paging;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Chat por publicación. Todo requiere token; los mensajes nuevos llegan en tiempo real por WebSocket ({@code /ws}). */
@RestController
@RequestMapping("/conversations")
public class ConversationController {

    static final int DEFAULT_MESSAGES_PAGE_SIZE = 30;

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    /** Sin {@code role}, las dos bandejas juntas: las que me escribieron y las que inicié. */
    @GetMapping("")
    public PageDTO<ConversationDTO> list(@RequestParam(required = false) ChatRole role,
                                         @RequestParam(required = false) Long publicationId,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "" + Paging.DEFAULT_PAGE_SIZE) int pageSize) {
        return conversationService.list(role, publicationId, page, pageSize);
    }

    /** 201 si el mensaje creó la conversación; 200 si ya existía y se sumó a ella. */
    @PostMapping("")
    public ResponseEntity<ConversationDTO> start(@Valid @RequestBody StartConversationDTOin request) {
        StartedConversation result = conversationService.start(request);
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(result.conversation());
    }

    @GetMapping("/{id}")
    public ConversationDTO get(@PathVariable Long id) {
        return conversationService.get(id);
    }

    @GetMapping("/{id}/messages")
    public MessagePageDTO messages(@PathVariable Long id,
                                   @RequestParam(required = false) Long before,
                                   @RequestParam(defaultValue = "" + DEFAULT_MESSAGES_PAGE_SIZE) int pageSize) {
        return conversationService.messages(id, before, pageSize);
    }

    @PostMapping("/{id}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageDTO send(@PathVariable Long id, @Valid @RequestBody MessageDTOin request) {
        return conversationService.send(id, request);
    }

    @PutMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@PathVariable Long id) {
        conversationService.markRead(id);
    }
}
