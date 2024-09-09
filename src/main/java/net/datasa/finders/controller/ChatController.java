package net.datasa.finders.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.datasa.finders.domain.dto.ChatMessageDTO;
import net.datasa.finders.service.ChatMessageService;

@Controller
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ChatController(ChatMessageService chatMessageService, SimpMessagingTemplate messagingTemplate) {
        this.chatMessageService = chatMessageService;
        this.messagingTemplate = messagingTemplate;
    }

    
    // 이전 채팅 메시지 로드 API
    @GetMapping("/chat/messages")
    @ResponseBody
    public List<ChatMessageDTO> getChatMessages(@RequestParam("chatroomId") int chatroomId) {
        return chatMessageService.getAllMessagesForChatroom(chatroomId);
    }
    
    @MessageMapping("/send")
    public void sendMessage(@Payload ChatMessageDTO chatMessage) {
        logger.info("Received message: {}", chatMessage); // 메시지 수신 로그 추가
        try {
            // 메시지를 데이터베이스에 저장
            chatMessageService.saveMessage(chatMessage);
            logger.info("Message saved with chatroom ID: {}", chatMessage.getChatroomId());

            // 특정 채팅방으로 메시지 전송
            messagingTemplate.convertAndSend("/topic/messages/" + chatMessage.getChatroomId(), chatMessage);
            logger.info("Message sent to chatroom: /topic/messages/{}", chatMessage.getChatroomId());
        } catch (Exception e) {
            logger.error("Failed to send message to chatroom: {}", chatMessage.getChatroomId(), e);
        }
    }
}
