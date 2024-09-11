package net.datasa.finders.controller;

import java.security.Principal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.datasa.finders.domain.dto.ChatMessageDTO;
import net.datasa.finders.service.ChatMessageService;

//채팅전용
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
    public void sendMessage(@Payload ChatMessageDTO chatMessage, 
                            @Header("simpUser") Principal principal) {
        logger.info("Received message: {}", chatMessage);

        // Principal로부터 현재 사용자 ID 설정
        String currentUserId = (principal != null) ? principal.getName() : "UnknownUser";

        // 받은 메시지에서 senderId를 현재 로그인된 사용자 ID로 설정
        chatMessage.setSenderId(currentUserId);

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
