package net.datasa.finders.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.datasa.finders.domain.entity.FreelancerPortfoliosEntity;
import net.datasa.finders.domain.entity.MemberEntity;

@Repository
public interface FreelancerPortfoliosRepository extends JpaRepository<FreelancerPortfoliosEntity, Integer> {

	List<FreelancerPortfoliosEntity> findByMember(MemberEntity memberEntity);

}
