package net.datasa.finders.repository;

import net.datasa.finders.domain.entity.ChatRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Integer> {
    Optional<ChatRoomEntity> findByProjectNum(int projectNum);
}
