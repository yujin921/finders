package net.datasa.finders.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.datasa.finders.domain.entity.ProjectEntity;

//채팅 전용
//채팅에서 project entity, dto를 사용하는 repository
@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, Integer> {

    // 프로젝트 제목으로 프로젝트 검색 (필요 시 커스텀 메서드 추가 가능)
    Optional<ProjectEntity> findByProjectName(String projectName);

    // 특정 멤버가 참여하고 있는 프로젝트를 가져오는 JPQL 쿼리
    @Query("SELECT p FROM ProjectEntity p JOIN p.members m WHERE m.memberId = :memberId")
    List<ProjectEntity> findProjectsByMemberId(@Param("memberId") String memberId);

}
