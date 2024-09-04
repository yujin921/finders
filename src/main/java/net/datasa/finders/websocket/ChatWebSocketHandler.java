package net.datasa.finders.websocket;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import net.datasa.finders.domain.dto.ChatMessageDTO;
import net.datasa.finders.service.ChatMessageService;

public class ChatWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private ChatMessageService chatMessageService;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String payload = message.getPayload();
        System.out.println("Received message: " + payload);

        // 메시지 저장 로직
        ChatMessageDTO chatMessageDTO = ChatMessageDTO.builder()
                .chatroomId(1) // 채팅방 ID는 실제 채팅방 ID로 설정
                .senderId("user") // 발신자 ID는 실제 사용자 ID로 설정
                .messageContent(payload)
                .sentTime(new java.sql.Timestamp(System.currentTimeMillis()))
                .build();

        chatMessageService.saveChatMessage(chatMessageDTO);

        // 클라이언트에 메시지 전송
        session.sendMessage(new TextMessage("Message received: " + payload));
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        System.out.println("Connection established with: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws IOException {
        System.out.println("Connection closed with: " + session.getId());
    }
}
