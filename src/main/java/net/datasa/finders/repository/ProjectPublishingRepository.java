package net.datasa.finders.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.domain.entity.ProjectPublishingEntity;

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
	
	Page<ProjectPublishingEntity> findByProjectTitleContainingOrProjectDescriptionContaining(String word, String word2, Pageable p);

	List<ProjectPublishingEntity> findByClientId(MemberEntity client); // MemberEntity 사용

	List<ProjectPublishingEntity> findAllByClientIdIn(Set<MemberEntity> clients);

}
