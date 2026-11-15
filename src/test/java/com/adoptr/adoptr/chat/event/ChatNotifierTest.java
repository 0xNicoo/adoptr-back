package com.adoptr.adoptr.chat.event;

import com.adoptr.adoptr.chat.dto.response.ChatEventDTO;
import com.adoptr.adoptr.chat.dto.response.MessageDTO;
import com.adoptr.adoptr.chat.model.ChatRole;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ChatNotifierTest {

    private final SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
    private final ChatNotifier notifier = new ChatNotifier(template);
    private final MessageDTO message =
            new MessageDTO(101L, 50L, ChatRole.INTERESTED, "Hola", LocalDateTime.of(2026, 11, 15, 12, 0));

    @Test
    void empujaElMensajeALaColaPersonalDeCadaParticipante() {
        notifier.onMessageSent(new MessageSentEvent(List.of(7L, 8L), message));

        verify(template).convertAndSendToUser("7", "/queue/chat", ChatEventDTO.message(message));
        verify(template).convertAndSendToUser("8", "/queue/chat", ChatEventDTO.message(message));
    }

    @Test
    void siFallaElAvisoAUnoIgualAvisaAlOtro() {
        doThrow(new MessageDeliveryException("caído"))
                .when(template).convertAndSendToUser(eq("7"), any(), any(Object.class));

        notifier.onMessageSent(new MessageSentEvent(List.of(7L, 8L), message));

        verify(template).convertAndSendToUser("8", "/queue/chat", ChatEventDTO.message(message));
    }
}
