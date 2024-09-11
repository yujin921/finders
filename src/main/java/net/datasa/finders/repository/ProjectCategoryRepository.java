package net.datasa.finders.repository;

import net.datasa.finders.domain.entity.ProjectPublishingEntity;
import net.datasa.finders.domain.entity.ProjectCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 게시판 관련 repository
 */

@Repository
public interface ProjectCategoryRepository extends JpaRepository<ProjectCategoryEntity, Integer> {
    List<ProjectCategoryEntity> findByProjectPublishingEntity(ProjectPublishingEntity entity);
}