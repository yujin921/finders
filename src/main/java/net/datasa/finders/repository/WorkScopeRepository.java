package net.datasa.finders.repository;

import net.datasa.finders.domain.entity.ProjectPublishingEntity;
import net.datasa.finders.domain.entity.WorkScopeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 게시판 관련 repository
 */

@Repository
public interface WorkScopeRepository extends JpaRepository<WorkScopeEntity, Integer> {
    List<WorkScopeEntity> findByProjectPublishingEntity(ProjectPublishingEntity entity);

}
