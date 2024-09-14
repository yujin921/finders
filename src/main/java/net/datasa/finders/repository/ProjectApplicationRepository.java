package net.datasa.finders.repository;

import net.datasa.finders.domain.entity.ApplicationResult;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.domain.entity.ProjectApplicationEntity;
import net.datasa.finders.domain.entity.ProjectPublishingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectApplicationRepository extends JpaRepository<ProjectApplicationEntity, Integer> {
    boolean existsByProjectNumAndFreelancer(ProjectPublishingEntity project, MemberEntity freelancer);
    Optional<ProjectApplicationEntity> findByProjectNumAndFreelancer(ProjectPublishingEntity project, MemberEntity freelancer);
    // 프로젝트 번호와 신청 상태에 따라 신청 목록을 가져오는 메서드
    List<ProjectApplicationEntity> findByProjectNumAndApplicationResult(ProjectPublishingEntity project, ApplicationResult applicationResult);

    List<ProjectApplicationEntity> findByProjectNum_ClientId(MemberEntity client);
}
