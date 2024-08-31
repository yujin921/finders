package net.datasa.finders.repository;

import net.datasa.finders.domain.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 회원 정보 Repository
 */

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, String> {

}