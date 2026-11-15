package com.adoptr.adoptr.shared.websocket;

import com.adoptr.adoptr.shared.security.CorsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket con STOMP en {@code /ws} y broker en memoria. Cada usuario recibe sus eventos en
 * {@code /user/queue/...}. El broker en memoria alcanza con una sola instancia del backend; con varias,
 * reemplazarlo por un broker externo (RabbitMQ, ActiveMQ) con {@code enableStompBrokerRelay}.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    public static final String ENDPOINT = "/ws";
    /** Heartbeats cada 10 s en los dos sentidos, para detectar conexiones caídas y que los proxies no las corten. */
    private static final long[] HEARTBEAT_MS = {10_000, 10_000};

    private final StompAuthInterceptor authInterceptor;
    private TaskScheduler heartbeatScheduler;

    public WebSocketConfig(StompAuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    /** El scheduler lo crea la propia configuración de STOMP; {@code @Lazy} evita la dependencia circular. */
    @Autowired
    public void setHeartbeatScheduler(@Lazy @Qualifier("messageBrokerTaskScheduler") TaskScheduler scheduler) {
        this.heartbeatScheduler = scheduler;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(ENDPOINT).setAllowedOrigins(CorsConfig.ALLOWED_ORIGINS);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/queue")
                .setHeartbeatValue(HEARTBEAT_MS)
                .setTaskScheduler(heartbeatScheduler);
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authInterceptor);
    }
}
