package net.datasa.finders.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.datasa.finders.domain.entity.ChatParticipantEntity;
import net.datasa.finders.domain.entity.ChatParticipantId;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipantEntity, ChatParticipantId> {
    // 추가적인 쿼리 메서드를 정의할 수 있습니다.
}