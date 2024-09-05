package net.datasa.finders.websocket;

import net.datasa.finders.domain.dto.ChatMessageDTO;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketHandler {

    // 채팅 메시지를 수신하는 엔드포인트
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessageDTO sendMessage(ChatMessageDTO message) {
        // 메시지 전송 시간 설정
        message.setSentTime(new java.sql.Timestamp(System.currentTimeMillis()));
        return message; // 받은 메시지를 그대로 리턴해서 브로드캐스트
    }

    // 사용자가 채팅방에 입장할 때 호출되는 메서드
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessageDTO addUser(ChatMessageDTO message, SimpMessageHeaderAccessor headerAccessor) {
        // 세션에 사용자 이름 저장
        headerAccessor.getSessionAttributes().put("username", message.getSenderId());
        // 입장 메시지 설정
        message.setMessageContent(message.getSenderId() + "님이 입장하셨습니다.");
        message.setSentTime(new java.sql.Timestamp(System.currentTimeMillis()));
        return message;
    }
}
