package net.datasa.finders.repository;

import net.datasa.finders.domain.entity.ChatRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Integer> {

    // projectNum으로 채팅방 조회 메서드 추가
    Optional<ChatRoomEntity> findByProjectNum(int projectNum);

    // 기존 메서드
    boolean existsByProjectNum(int projectNum);

    // 채팅방 ID로 채팅방 조회
    Optional<ChatRoomEntity> findById(int chatroomId);
}
