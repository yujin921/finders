package net.datasa.finders.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.datasa.finders.domain.dto.TaskDateRangeDTO;
import net.datasa.finders.domain.entity.FunctionTitleEntity;
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
	
	boolean existsByProjectPublishingEntity_ProjectNumAndFunctionTitleEntity_TitleName(int projectNum, String titleName);
	
	boolean existsByProjectPublishingEntity_ProjectNumAndTaskTitle(int projectNum, String taskTitle);
	
	// 주어진 프로젝트 번호와 기능 제목 ID로 TaskManagementEntity 존재 여부를 확인
    boolean existsByProjectPublishingEntity_ProjectNumAndFunctionTitleEntity_FunctionTitleId(int projectNum, int functionTitleId);
	
    // 특정 프로젝트의 모든 FunctionTitleEntity를 찾기
    List<FunctionTitleEntity> findDistinctFunctionTitlesByProjectPublishingEntity_ProjectNum(int projectNum);

    @Query("SELECT t.functionTitleEntity.functionTitleId FROM TaskManagementEntity t WHERE t.taskId = :taskId")
    Integer findFunctionTitleIdByTaskId(@Param("taskId") int taskId);

    @Query("SELECT COUNT(t) FROM TaskManagementEntity t WHERE t.functionTitleEntity.functionTitleId = :functionTitleId")
    long countTasksByFunctionTitleId(@Param("functionTitleId") int functionTitleId);

    @Query("SELECT t FROM TaskManagementEntity t WHERE t.functionTitleEntity.functionTitleId = :functionTitleId")
    List<TaskManagementEntity> findTasksByFunctionTitleId(@Param("functionTitleId") int functionTitleId);
    
}
