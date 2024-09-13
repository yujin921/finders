package net.datasa.finders.repository;

import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.domain.entity.ProjectApplicationEntity;
import net.datasa.finders.domain.entity.ProjectPublishingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectApplicationRepository extends JpaRepository<ProjectApplicationEntity, Integer> {
    boolean existsByProjectNumAndFreelancer(ProjectPublishingEntity project, MemberEntity freelancer);
    Optional<ProjectApplicationEntity> findByProjectNumAndFreelancer(ProjectPublishingEntity project, MemberEntity freelancer);

}
