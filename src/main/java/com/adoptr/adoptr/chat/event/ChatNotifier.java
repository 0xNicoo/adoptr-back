package com.adoptr.adoptr.chat.event;

import com.adoptr.adoptr.chat.dto.response.ChatEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Empuja los mensajes nuevos por WebSocket a la cola personal de cada participante ({@code /user/queue/chat}).
 * Corre después del commit: nunca avisa de un mensaje que no quedó guardado. Si el envío falla, el mensaje
 * igual está en la base y aparece al recargar el historial.
 */
@Component
public class ChatNotifier {

    public static final String USER_DESTINATION = "/queue/chat";

    private static final Logger log = LoggerFactory.getLogger(ChatNotifier.class);

    private final SimpMessagingTemplate messagingTemplate;

    public ChatNotifier(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMessageSent(MessageSentEvent event) {
        ChatEventDTO payload = ChatEventDTO.message(event.message());
        for (Long userId : event.recipientUserIds()) {
            try {
                // El nombre del usuario de la sesión STOMP es su id interno (ver StompAuthInterceptor).
                messagingTemplate.convertAndSendToUser(String.valueOf(userId), USER_DESTINATION, payload);
            } catch (MessagingException e) {
                log.warn("No se pudo avisar el mensaje {} al usuario {}", event.message().id(), userId, e);
            }
        }
    }
}
