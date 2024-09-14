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

    @Query(value = "SELECT p.* FROM project_publishing p JOIN team t ON p.project_num = t.project_num WHERE t.member_id = :memberId", nativeQuery = true)
    List<ProjectEntity> findProjectsByMemberId(@Param("memberId") String memberId);
    // ProjectRepository.java
    @Query(value = "SELECT p.* FROM project_publishing p JOIN team t ON p.project_num = t.project_num WHERE t.member_id = :memberId", nativeQuery = true)
    List<ProjectEntity> findProjectsByMemberIdNative(@Param("memberId") String memberId);
}
