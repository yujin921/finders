package net.datasa.finders.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.datasa.finders.domain.entity.FreelancerEntity;
import net.datasa.finders.domain.entity.MemberEntity;

@Repository

public interface FreelancerRepository extends JpaRepository<FreelancerEntity, Integer> {
	
	Optional<FreelancerEntity> findByMember(MemberEntity member);
}
