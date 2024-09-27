package net.datasa.finders.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.datasa.finders.domain.entity.CalendarEventEntity;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEventEntity, Integer> {

	// 프로젝트 번호로 일정 조회
    List<CalendarEventEntity> findByProject_ProjectNum(Integer projectNum);
	
}
