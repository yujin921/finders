package net.datasa.finders.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.datasa.finders.domain.entity.ChatRoomEntity;

//채팅전용
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Integer> {

    // projectNum으로 채팅방 조회 메서드 추가
    Optional<ChatRoomEntity> findByProjectNum(int projectNum);

    // 기존 메서드
    boolean existsByProjectNum(int projectNum);

    // 채팅방 ID로 채팅방 조회
    Optional<ChatRoomEntity> findById(int chatroomId);

    // ProjectEntity와 JOIN하여 프로젝트 제목을 포함한 채팅방 조회 메서드 추가
    @Query("SELECT c FROM ChatRoomEntity c JOIN FETCH c.project p WHERE c.chatroomId = :chatroomId")
    Optional<ChatRoomEntity> findByIdWithProject(@Param("chatroomId") int chatroomId);

    // ChatRoom과 Project를 JOIN하여 데이터 가져오기
    @Query("SELECT c FROM ChatRoomEntity c JOIN c.project p WHERE p.projectNum = c.projectNum")
    List<ChatRoomEntity> findAllWithProject();

}
