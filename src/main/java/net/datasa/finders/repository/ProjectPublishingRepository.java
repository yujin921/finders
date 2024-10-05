package net.datasa.finders.repository;

import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.domain.entity.ProjectEntity;
import net.datasa.finders.domain.entity.ProjectPublishingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 게시판 관련 repository
 */

@Repository
public interface ProjectPublishingRepository extends JpaRepository<ProjectPublishingEntity, Integer> {
	@Query("SELECT p FROM ProjectPublishingEntity p WHERE p.projectNum IN :projectNums")
    List<ProjectPublishingEntity> findAllByProjectNumIn(@Param("projectNums") List<Integer> projectNums);

	ProjectPublishingEntity findByProjectNum(Integer integer);

	List<ProjectPublishingEntity> findByClientIdAndProjectStatus(MemberEntity memberEntity, boolean b);

	List<ProjectPublishingEntity> findByProjectTitleContainingOrProjectDescriptionContaining(String word, String word2);


}
