package net.datasa.finders.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.domain.entity.RoleName;

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

    // Fetch join을 사용하여 member와 연관된 project들을 강제로 로딩
    @Query("SELECT m FROM MemberEntity m LEFT JOIN FETCH m.projects WHERE m.memberId = :memberId")
    Optional<MemberEntity> findByIdWithProjects(@Param("memberId") String memberId);
    // 프로젝트 번호와 클라이언트 ID가 아닌 프리랜서들을 가져오는 쿼리 추가
    @Query("SELECT m FROM MemberEntity m JOIN m.projects p WHERE p.projectNum = :projectNum AND m.memberId <> :clientId")
    List<MemberEntity> findByProjectNumAndNotClientId(@Param("projectNum") int projectNum, @Param("clientId") String clientId);

    // 특정 프로젝트 번호와 연관된 멤버들을 찾는 메서드
    @Query("SELECT m FROM MemberEntity m JOIN m.projects p WHERE p.projectNum = :projectNum")
    List<MemberEntity> findByProjectNum(@Param("projectNum") int projectNum);
}

