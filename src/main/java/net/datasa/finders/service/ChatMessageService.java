package net.datasa.finders.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.datasa.finders.domain.dto.ChatMessageDTO;
import net.datasa.finders.domain.entity.ChatMessageEntity;
import net.datasa.finders.repository.ChatMessageRepository;
import net.datasa.finders.repository.ChatRoomRepository;

//채팅전용
@Service
public class ChatMessageService {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageService.class);

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Autowired
    public ChatMessageService(ChatMessageRepository chatMessageRepository, ChatRoomRepository chatRoomRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatRoomRepository = chatRoomRepository;
    }

    // 메시지 저장 메서드
    @Transactional
    public void saveMessage(ChatMessageDTO chatMessageDTO) {
        logger.info("Attempting to save message: {}", chatMessageDTO); // DTO 로그 추가

        // chatroom_id가 존재하는지 확인
        if (!chatRoomRepository.existsById(chatMessageDTO.getChatroomId())) {
            logger.error("Chat room does not exist for chatroom_id: {}", chatMessageDTO.getChatroomId());
            throw new IllegalArgumentException("Chat room does not exist for chatroom_id: " + chatMessageDTO.getChatroomId());
        }

        // 메시지 검증 (예시: 빈 메시지 방지)
        if (chatMessageDTO.getMessageContents() == null || chatMessageDTO.getMessageContents().trim().isEmpty()) {
            logger.error("Message content is empty for chatroom_id: {}", chatMessageDTO.getChatroomId());
            throw new IllegalArgumentException("Message content cannot be empty.");
        }

        try {
            ChatMessageEntity chatMessage = ChatMessageEntity.builder()
                    .chatroomId(chatMessageDTO.getChatroomId())
                    .senderId(chatMessageDTO.getSenderId())
                    .messageContents(chatMessageDTO.getMessageContents())
                    .sendTime(chatMessageDTO.getSendTime())
                    .build();

            logger.info("Saving message entity: {}", chatMessage); // 엔티티 저장 직전 로그
            chatMessageRepository.save(chatMessage);
            logger.info("Message saved successfully for chatroom_id: {}", chatMessageDTO.getChatroomId());
        } catch (Exception e) {
            logger.error("Failed to save message for chatroom_id: {}. Exception: {}", chatMessageDTO.getChatroomId(), e.getMessage(), e);
            throw new RuntimeException("Failed to save message.", e);
        }
    }

    public List<ChatMessageDTO> getAllMessagesForChatroom(int chatroomId) {
        List<ChatMessageEntity> entities = chatMessageRepository.findAllByChatroomId(chatroomId);
        return entities.stream()
                .map(entity -> ChatMessageDTO.builder()
                        .messageId(entity.getMessageId())
                        .chatroomId(entity.getChatroomId())
                        .senderId(entity.getSenderId())
                        .messageContents(entity.getMessageContents())
                        .sendTime(entity.getSendTime())
                        .build())
                .collect(Collectors.toList());
    }
    
    // 추가된 메서드: 특정 채팅방의 메시지 목록 조회
    public List<ChatMessageEntity> getMessagesByChatroomId(Long chatroomId) {
        return chatMessageRepository.findAllByChatroomId(chatroomId.intValue());
    }
}
