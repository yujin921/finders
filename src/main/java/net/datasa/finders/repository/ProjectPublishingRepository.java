package net.datasa.finders.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.datasa.finders.domain.entity.ProjectPublishingEntity;

/**
 * 게시판 관련 repository
 */

@Repository
public interface ProjectPublishingRepository extends JpaRepository<ProjectPublishingEntity, Integer> {
	@Query("SELECT p FROM ProjectPublishingEntity p WHERE p.projectNum IN " +
	           "(SELECT pa.projectNum FROM ProjectApplicationEntity pa " +
	           "WHERE pa.freelancer.member.memberId = :freelancerId AND pa.applicationResult = net.datasa.finders.domain.entity.ApplicationResult.ACCEPTED)")
	    List<ProjectPublishingEntity> findAcceptedProjectsByFreelancerId(@Param("freelancerId") String freelancerId);
	
}
