package net.datasa.finders.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.domain.entity.TaskNotificationsEntity;

@Repository
public interface TaskNotificationsRepository extends JpaRepository<TaskNotificationsEntity, Integer> {

	// recipientId로 알림 목록을 찾는 메서드
    List<TaskNotificationsEntity> findByRecipientId(MemberEntity recipient);

}
