package net.datasa.finders.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.datasa.finders.domain.dto.ChatMessageDTO;
import net.datasa.finders.domain.dto.ChatParticipantDTO;
import net.datasa.finders.service.ChatMessageService;
import net.datasa.finders.service.ChatParticipantService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatParticipantService chatParticipantService;
    private final ChatMessageService chatMessageService; // Updated service name

    @Autowired
    public ChatController(ChatParticipantService chatParticipantService, ChatMessageService chatMessageService) {
        this.chatParticipantService = chatParticipantService;
        this.chatMessageService = chatMessageService; // Updated service name
    }

    // 채팅방에 참가자 추가
    @PostMapping("/participant")
    public ResponseEntity<ChatParticipantDTO> addParticipant(@RequestBody ChatParticipantDTO chatParticipantDTO) {
        ChatParticipantDTO addedParticipant = chatParticipantService.saveChatParticipant(chatParticipantDTO);
        return ResponseEntity.ok(addedParticipant);
    }

    // 특정 채팅방의 모든 참가자 조회
    @GetMapping("/participants/{chatroomId}")
    public ResponseEntity<List<ChatParticipantDTO>> getParticipants(@PathVariable int chatroomId) {
        List<ChatParticipantDTO> participants = chatParticipantService.getAllParticipantsForChatroom(chatroomId);
        return ResponseEntity.ok(participants);
    }

    // 메시지 전송
    @PostMapping("/message")
    public ResponseEntity<ChatMessageDTO> sendMessage(@RequestBody ChatMessageDTO chatMessageDTO) {
        ChatMessageDTO sentMessage = chatMessageService.saveChatMessage(chatMessageDTO);
        // 웹소켓을 통해 메시지를 전송하는 로직을 추가할 수 있습니다.
        return ResponseEntity.ok(sentMessage);
    }

    // 특정 채팅방의 모든 메시지 조회
    @GetMapping("/messages/{chatroomId}")
    public ResponseEntity<List<ChatMessageDTO>> getMessages(@PathVariable int chatroomId) {
        List<ChatMessageDTO> messages = chatMessageService.getAllMessagesForChatroom(chatroomId);
        return ResponseEntity.ok(messages);
    }
}
