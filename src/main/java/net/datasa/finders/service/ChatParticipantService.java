package net.datasa.finders.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.datasa.finders.domain.dto.ChatParticipantDTO;
import net.datasa.finders.domain.entity.ChatParticipantEntity;
import net.datasa.finders.domain.entity.ChatParticipantId;
import net.datasa.finders.repository.ChatParticipantRepository;

@Service
public class ChatParticipantService {

    private final ChatParticipantRepository chatParticipantRepository;

    @Autowired
    public ChatParticipantService(ChatParticipantRepository chatParticipantRepository) {
        this.chatParticipantRepository = chatParticipantRepository;
    }

    // Create or Update a ChatParticipant
    public ChatParticipantDTO saveChatParticipant(ChatParticipantDTO chatParticipantDTO) {
        ChatParticipantEntity entity = ChatParticipantEntity.builder()
                .chatroomId(chatParticipantDTO.getChatroomId())
                .participantId(chatParticipantDTO.getParticipantId())
                .joinedTime(chatParticipantDTO.getJoinedTime())
                .build();
        ChatParticipantEntity savedEntity = chatParticipantRepository.save(entity);
        return toDTO(savedEntity);
    }

    // Retrieve a ChatParticipant by ID
    public ChatParticipantDTO getChatParticipant(int chatroomId, String participantId) {
        ChatParticipantId id = new ChatParticipantId(chatroomId, participantId);
        Optional<ChatParticipantEntity> entity = chatParticipantRepository.findById(id);
        return entity.map(this::toDTO).orElse(null);
    }

    // Retrieve all ChatParticipants for a specific chatroom
    public List<ChatParticipantDTO> getAllParticipantsForChatroom(int chatroomId) {
        List<ChatParticipantEntity> entities = chatParticipantRepository.findAll(); // Add filter logic if necessary
        return entities.stream()
                .filter(entity -> entity.getChatroomId() == chatroomId)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Convert Entity to DTO
    private ChatParticipantDTO toDTO(ChatParticipantEntity entity) {
        return ChatParticipantDTO.builder()
                .chatroomId(entity.getChatroomId())
                .participantId(entity.getParticipantId())
                .joinedTime(entity.getJoinedTime())
                .build();
    }
}
