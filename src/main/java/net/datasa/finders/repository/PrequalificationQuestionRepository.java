package net.datasa.finders.repository;

import net.datasa.finders.domain.entity.PrequalificationQuestionEntity;
import net.datasa.finders.domain.entity.ProjectPublishingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 게시판 관련 repository
 */

@Repository
public interface PrequalificationQuestionRepository extends JpaRepository<PrequalificationQuestionEntity, Integer> {
    List<PrequalificationQuestionEntity> findByProjectPublishingEntity(ProjectPublishingEntity projectPublishingEntity);

}
