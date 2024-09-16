package net.datasa.finders.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.datasa.finders.domain.dto.TaskDateRangeDTO;
import net.datasa.finders.domain.entity.TaskManagementEntity;

@Repository
public interface TaskManagementRepository extends JpaRepository<TaskManagementEntity, Integer> {
	// 프로젝트 번호를 기준으로 업무를 조회하는 메서드
	List<TaskManagementEntity> findByProjectPublishingEntity_ProjectNum(int projectNum);
	
	@Query("SELECT new net.datasa.finders.domain.dto.TaskDateRangeDTO(" +
	           "MIN(t.taskStartDate), MAX(t.taskEndDate)) " +
	           "FROM TaskManagementEntity t " +
	           "WHERE t.projectPublishingEntity.projectNum = :projectNum")
	Optional<TaskDateRangeDTO> findTaskDateRangeByProjectNum(@Param("projectNum") int projectNum);
}
