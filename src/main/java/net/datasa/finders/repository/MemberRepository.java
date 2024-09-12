package net.datasa.finders.repository;

import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.domain.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, String> {

    MemberEntity findByMemberIdAndRoleName(String memberId, RoleName roleName);

    // 특정 프로젝트에 참여한 멤버를 찾는 메서드
    List<MemberEntity> findByProjects_ProjectNum(int projectNum);

    // 특정 멤버 ID와 프로젝트 번호를 기반으로 멤버를 찾는 메서드 추가
    List<MemberEntity> findByMemberIdAndProjects_ProjectNum(String memberId, int projectNum);

    // team 테이블의 데이터를 확인하기 위한 임시 메서드
    @Query(value = "SELECT project_num, member_id FROM team", nativeQuery = true)
    List<Object[]> findTeamEntries();

    Optional<MemberEntity> findByMemberId(String memberId);
}
