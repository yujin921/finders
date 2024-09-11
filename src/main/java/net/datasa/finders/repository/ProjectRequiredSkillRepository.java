package net.datasa.finders.repository;

import net.datasa.finders.domain.entity.ProjectPublishingEntity;
import net.datasa.finders.domain.entity.ProjectRequiredSkillEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 게시판 관련 repository
 */

@Repository
public interface ProjectRequiredSkillRepository extends JpaRepository<ProjectRequiredSkillEntity, Integer> {
    List<ProjectRequiredSkillEntity> findByProjectPublishingEntity(ProjectPublishingEntity entity);
}
