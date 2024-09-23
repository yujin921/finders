package net.datasa.finders.repository;

import net.datasa.finders.domain.entity.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<TeamEntity, Integer> { // 복합 키 클래스 설정

    // 특정 프로젝트 번호에 해당하는 팀 구성원 조회
    List<TeamEntity> findByProjectNum(Integer projectNum);

    // 특정 member_id가 포함된 팀 조회 메서드 추가
    List<TeamEntity> findByMemberId(String memberId);

    boolean existsByProjectNumAndMemberId(int projectNum, String memberId);
}
