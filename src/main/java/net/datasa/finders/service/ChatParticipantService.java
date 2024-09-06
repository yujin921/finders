package net.datasa.finders.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import net.datasa.finders.domain.entity.ChatParticipantEntity;
import net.datasa.finders.repository.ChatParticipantRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatParticipantService {

    private final ChatParticipantRepository chatParticipantRepository;

    @Autowired
    public ChatParticipantService(ChatParticipantRepository chatParticipantRepository) {
        this.chatParticipantRepository = chatParticipantRepository;
    }

    // 채팅방에 참여자를 추가하는 메서드
    @Transactional
    public void addParticipant(Integer chatroomId, String participantId) {
        try {
            ChatParticipantEntity participant = ChatParticipantEntity.builder()
                    .chatroomId(chatroomId)
                    .participantId(participantId)
                    .joinedTime(LocalDateTime.now())
                    .build();

            chatParticipantRepository.save(participant);
        } catch (Exception e) {
            // 필요한 경우 예외를 로깅하거나 커스텀 예외로 변환하여 처리
            throw new RuntimeException("참여자 추가 중 오류 발생", e);
        }
    }

    // 특정 채팅방의 참여자 목록을 조회하는 메서드
    public List<ChatParticipantEntity> getParticipantsByChatroomId(Integer chatroomId) {
        try {
            return chatParticipantRepository.findByChatroomId(chatroomId);
        } catch (Exception e) {
            // 필요한 경우 예외를 로깅하거나 커스텀 예외로 변환하여 처리
            throw new RuntimeException("참여자 조회 중 오류 발생", e);
        }
    }
}
