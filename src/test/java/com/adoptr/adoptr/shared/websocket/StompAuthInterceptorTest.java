package com.adoptr.adoptr.shared.websocket;

import com.adoptr.adoptr.shared.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.security.Principal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class StompAuthInterceptorTest {

    private final JwtUtil jwtUtil = new JwtUtil("test-secret-test-secret-test-secret-123456", 3_600_000);
    private final StompAuthInterceptor interceptor = new StompAuthInterceptor(jwtUtil);
    private final MessageChannel channel = mock(MessageChannel.class);

    private static Message<byte[]> frame(StompHeaderAccessor accessor) {
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private static Message<byte[]> connect(String authorization) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        if (authorization != null) {
            accessor.setNativeHeader("Authorization", authorization);
        }
        return frame(accessor);
    }

    private static Message<byte[]> subscribe(String destination, Principal user) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination(destination);
        accessor.setUser(user);
        return frame(accessor);
    }

    private static Principal user(long id) {
        return new UsernamePasswordAuthenticationToken(id, null, List.of());
    }

    @Test
    void conectarConUnJwtValidoDejaElIdComoNombreDelUsuario() {
        Message<?> result = interceptor.preSend(connect("Bearer " + jwtUtil.generateToken(8L)), channel);

        Principal user = StompHeaderAccessor.wrap(result).getUser();
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("8");
    }

    @Test
    void conectarSinTokenOConUnoInvalidoSeRechaza() {
        for (String authorization : new String[]{null, "Basic abc", "Bearer no-es-un-jwt"}) {
            assertThatThrownBy(() -> interceptor.preSend(connect(authorization), channel))
                    .isInstanceOf(MessageDeliveryException.class)
                    .hasMessage("UNAUTHENTICATED");
        }
    }

    @Test
    void conectarConUnTokenFirmadoConOtroSecretoSeRechaza() {
        JwtUtil other = new JwtUtil("otro-secreto-otro-secreto-otro-secreto-99", 3_600_000);

        assertThatThrownBy(() -> interceptor.preSend(connect("Bearer " + other.generateToken(8L)), channel))
                .isInstanceOf(MessageDeliveryException.class)
                .hasMessage("UNAUTHENTICATED");
    }

    @Test
    void sePuedeSuscribirASuColaPersonal() {
        Message<byte[]> message = subscribe("/user/queue/chat", user(8L));

        assertThat(interceptor.preSend(message, channel)).isSameAs(message);
    }

    @Test
    void noSePuedeSuscribirSinHaberseAutenticado() {
        assertThatThrownBy(() -> interceptor.preSend(subscribe("/user/queue/chat", null), channel))
                .isInstanceOf(MessageDeliveryException.class)
                .hasMessage("UNAUTHENTICATED");
    }

    @Test
    void noSePuedeSuscribirALaColaDeOtraSesionNiATopicos() {
        for (String destination : new String[]{"/queue/chat-user1234", "/topic/chat", "/user/7/queue/chat"}) {
            assertThatThrownBy(() -> interceptor.preSend(subscribe(destination, user(8L)), channel))
                    .isInstanceOf(MessageDeliveryException.class)
                    .hasMessage("ACCESS_DENIED");
        }
    }

    @Test
    void elCanalEsSoloDeRecepcion() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setDestination("/app/chat");
        accessor.setUser(user(8L));

        assertThatThrownBy(() -> interceptor.preSend(frame(accessor), channel))
                .isInstanceOf(MessageDeliveryException.class)
                .hasMessage("ACCESS_DENIED");
    }
}
