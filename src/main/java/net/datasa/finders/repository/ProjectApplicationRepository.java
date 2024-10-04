package net.datasa.finders.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.datasa.finders.domain.entity.ApplicationResult;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.domain.entity.ProjectApplicationEntity;
import net.datasa.finders.domain.entity.ProjectPublishingEntity;

@Repository
public interface ProjectApplicationRepository extends JpaRepository<ProjectApplicationEntity, Integer> {
    boolean existsByProjectNumAndFreelancer(ProjectPublishingEntity project, MemberEntity freelancer);
    Optional<ProjectApplicationEntity> findByProjectNumAndFreelancer(ProjectPublishingEntity project, MemberEntity freelancer);

    List<ProjectApplicationEntity> findByProjectNumAndProjectNum_ClientId(ProjectPublishingEntity projectNum, MemberEntity clientId);
    
	List<ProjectApplicationEntity> findByFreelancerAndApplicationResult(MemberEntity memberEntity,
			ApplicationResult pending);
	
	List<ProjectApplicationEntity> findByFreelancer(MemberEntity memberEntity);
}
