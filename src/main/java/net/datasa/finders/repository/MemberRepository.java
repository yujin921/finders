package net.datasa.finders.repository;

import net.datasa.finders.domain.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, String> {

    // 특정 프로젝트에 참여한 멤버를 찾는 메서드
    List<MemberEntity> findByProjects_ProjectNum(int projectNum);
}
