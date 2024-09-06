package net.datasa.finders.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.datasa.finders.domain.dto.ChatMessageDTO;
import net.datasa.finders.domain.entity.ChatMessageEntity;
import net.datasa.finders.repository.ChatMessageRepository;

@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    @Autowired
    public ChatMessageService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    // 특정 채팅방의 모든 메시지 조회
    public List<ChatMessageDTO> getAllMessagesForChatroom(int chatroomId) {
        List<ChatMessageEntity> entities = chatMessageRepository.findAllByChatroomId(chatroomId);
        return entities.stream()
                .map(entity -> ChatMessageDTO.builder()
                        .messageId(entity.getMessageId())
                        .chatroomId(entity.getChatroomId())
                        .senderId(entity.getSenderId())
                        .messageContent(entity.getMessageContents())
                        .sendTime(entity.getSendTime()) // 필드 이름을 `sendTime`으로 변경
                        .build())
                .collect(Collectors.toList());
    }

    // 메시지 저장 메서드
    @Transactional
    public void saveMessage(ChatMessageDTO chatMessageDTO) {
        ChatMessageEntity chatMessage = ChatMessageEntity.builder()
                .chatroomId(chatMessageDTO.getChatroomId())
                .senderId(chatMessageDTO.getSenderId())
                .messageContents(chatMessageDTO.getMessageContent()) // 필드 이름을 `messageContent`로 변경
                .sendTime(chatMessageDTO.getSendTime()) // 필드 이름을 `sendTime`으로 변경
                .build();
        chatMessageRepository.save(chatMessage);
    }
}

