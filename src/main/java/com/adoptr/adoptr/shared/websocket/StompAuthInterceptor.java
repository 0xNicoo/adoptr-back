package com.adoptr.adoptr.shared.websocket;

import com.adoptr.adoptr.shared.exception.error.CommonError;
import com.adoptr.adoptr.shared.security.JwtUtil;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Autoriza el canal STOMP. El navegador no puede mandar headers en el handshake de WebSocket, así que el JWT
 * viaja en el frame {@code CONNECT} ({@code Authorization: Bearer ...}). El usuario de la sesión queda con
 * el id interno como nombre, igual que el principal del filtro HTTP.
 * <p>
 * El canal es solo de recepción: se puede suscribir únicamente a las colas personales ({@code /user/queue/...});
 * los mensajes se envían por HTTP, con las mismas validaciones y errores que el resto de la API.
 * Un rechazo responde un frame {@code ERROR} con el código en {@code message}.
 */
@Component
public class StompAuthInterceptor implements ChannelInterceptor {

    static final String AUTHORIZATION = "Authorization";
    static final String BEARER_PREFIX = "Bearer ";
    static final String USER_QUEUE_PREFIX = "/user/queue/";

    private final JwtUtil jwtUtil;

    public StompAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }
        switch (accessor.getCommand()) {
            case CONNECT, STOMP -> accessor.setUser(authenticate(accessor));
            case SUBSCRIBE -> authorizeSubscription(accessor);
            case SEND -> throw denied(CommonError.ACCESS_DENIED);
            default -> {
                // UNSUBSCRIBE, DISCONNECT, ACK, etc.: no necesitan control propio.
            }
        }
        return message;
    }

    private UsernamePasswordAuthenticationToken authenticate(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader(AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw denied(CommonError.UNAUTHENTICATED);
        }
        Long userId = jwtUtil.extractUserId(header.substring(BEARER_PREFIX.length()))
                .orElseThrow(() -> denied(CommonError.UNAUTHENTICATED));
        // getName() de este token devuelve el id como texto: es el nombre que usa convertAndSendToUser.
        return new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        if (accessor.getUser() == null) {
            throw denied(CommonError.UNAUTHENTICATED);
        }
        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith(USER_QUEUE_PREFIX)) {
            throw denied(CommonError.ACCESS_DENIED);
        }
    }

    private static MessageDeliveryException denied(CommonError error) {
        return new MessageDeliveryException(error.getCode());
    }
}
