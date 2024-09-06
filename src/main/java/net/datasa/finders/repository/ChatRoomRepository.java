package net.datasa.finders.repository;

import net.datasa.finders.domain.entity.ChatRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Integer> {

    // 특정 프로젝트 번호에 해당하는 채팅방이 존재하는지 확인하는 메서드
    boolean existsByProjectNum(Integer projectNum);

    // chatroom_id 목록을 통해 채팅방 정보 조회
    List<ChatRoomEntity> findByChatroomIdIn(List<Integer> chatroomIds);
}
