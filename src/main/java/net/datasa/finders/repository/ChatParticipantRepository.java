package net.datasa.finders.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import net.datasa.finders.domain.entity.ChatParticipantEntity;
import net.datasa.finders.domain.entity.ChatParticipantId;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipantEntity, ChatParticipantId> {

    // 특정 member_id가 참여 중인 채팅방 목록 조회
    List<ChatParticipantEntity> findByParticipantId(String participantId);

    // 특정 채팅방의 참여자 목록 조회 메서드 추가
    List<ChatParticipantEntity> findByChatroomId(Integer chatroomId);
    // chatroomId와 participantId로 존재 여부 확인
    boolean existsByChatroomIdAndParticipantId(int chatroomId, String participantId);
}
