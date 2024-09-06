package net.datasa.finders.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.datasa.finders.domain.entity.ChatRoomEntity;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Integer> {
    boolean existsByProjectNum(int projectNum);

    // 채팅방 ID로 채팅방 조회
    Optional<ChatRoomEntity> findById(int chatroomId);
}
