package net.datasa.finders.websocket;


import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // 브로커는 /topic 하위의 메시지를 전송합니다.
        config.setApplicationDestinationPrefixes("/app"); // 메시지 전송 시 /app을 prefix로 사용합니다.
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat") // WebSocket의 엔드포인트 설정
                .setAllowedOrigins("*") // 모든 도메인에서 접근 허용 (보안을 위해 필요한 설정을 추가 가능)
                .withSockJS(); // SockJS 사용
    }
}
