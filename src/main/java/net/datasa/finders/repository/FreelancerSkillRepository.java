package net.datasa.finders.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.datasa.finders.domain.entity.FreelancerEntity;
import net.datasa.finders.domain.entity.FreelancerSkillEntity;
import net.datasa.finders.domain.entity.MemberEntity;

@Repository
public interface FreelancerSkillRepository extends JpaRepository<FreelancerSkillEntity, Integer> {

		void deleteByFreelancerId(MemberEntity freelancer);

		List<FreelancerSkillEntity> findByFreelancerId(MemberEntity memberEntity);
}
