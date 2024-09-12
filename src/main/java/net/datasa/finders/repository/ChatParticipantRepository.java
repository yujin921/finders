package net.datasa.finders.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import net.datasa.finders.domain.entity.ChatParticipantEntity;
import net.datasa.finders.domain.entity.ChatParticipantId;

//채팅전용
@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipantEntity, ChatParticipantId> {

    // 특정 member_id가 참여 중인 채팅방 목록 조회
    List<ChatParticipantEntity> findByParticipantId(String participantId);

    // 특정 채팅방의 참여자 목록 조회 메서드 추가
    List<ChatParticipantEntity> findByChatroomId(Integer chatroomId);
    // chatroomId와 participantId로 존재 여부 확인
    boolean existsByChatroomIdAndParticipantId(int chatroomId, String participantId);

    // chatroomId와 participantId를 기준으로 삭제
    @Modifying
    @Transactional
    int deleteByChatroomIdAndParticipantId(int chatroomId, String participantId);

    // 채팅방의 참가자 수를 조회하는 메서드
    int countByChatroomId(int chatroomId);  // 필드 이름과 메서드 이름이 일치해야 함
}
