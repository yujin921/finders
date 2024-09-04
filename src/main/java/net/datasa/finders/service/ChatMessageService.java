package net.datasa.finders.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    // Create or Update a ChatMessage
    public ChatMessageDTO saveChatMessage(ChatMessageDTO chatMessageDTO) {
        ChatMessageEntity entity = ChatMessageEntity.builder()
                .chatroomId(chatMessageDTO.getChatroomId())
                .messageId(chatMessageDTO.getMessageId())
                .senderId(chatMessageDTO.getSenderId())
                .messageContents(chatMessageDTO.getMessageContent()) // DTO와 Entity 필드 이름 일치
                .sendTime(chatMessageDTO.getSentTime()) // DTO와 Entity 필드 이름 일치
                .build();
        ChatMessageEntity savedEntity = chatMessageRepository.save(entity);
        return toDTO(savedEntity);
    }

    // Retrieve a ChatMessage by ID
    public ChatMessageDTO getChatMessage(int messageId) {
        Optional<ChatMessageEntity> entity = chatMessageRepository.findById(messageId);
        return entity.map(this::toDTO).orElse(null);
    }

    // Retrieve all messages for a specific chatroom
    public List<ChatMessageDTO> getAllMessagesForChatroom(int chatroomId) {
        List<ChatMessageEntity> entities = chatMessageRepository.findByChatroomId(chatroomId);
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Convert Entity to DTO
    private ChatMessageDTO toDTO(ChatMessageEntity entity) {
        return ChatMessageDTO.builder()
                .chatroomId(entity.getChatroomId())
                .messageId(entity.getMessageId())
                .senderId(entity.getSenderId())
                .messageContent(entity.getMessageContents()) // DTO와 Entity 필드 이름 일치
                .sentTime(entity.getSendTime()) // DTO와 Entity 필드 이름 일치
                .build();
    }
}
