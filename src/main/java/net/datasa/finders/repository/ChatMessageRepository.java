package net.datasa.finders.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.datasa.finders.domain.entity.ChatMessageEntity;

//채팅전용
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Integer> {
    List<ChatMessageEntity> findAllByChatroomId(int chatroomId);

    // 특정 채팅방의 모든 메시지를 삭제하는 메서드
    void deleteByChatroomId(int chatroomId);
    
    @Query("SELECT COUNT(m) FROM ChatMessageEntity m WHERE m.chatroomId = :chatroomId AND m.sendTime > :lastReadTime")
    int countNewMessagesAfter(@Param("chatroomId") int chatroomId, @Param("lastReadTime") Timestamp lastReadTime);

}
