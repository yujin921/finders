package net.datasa.finders.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.datasa.finders.domain.entity.FreelancerEntity;
import net.datasa.finders.domain.entity.MemberEntity;

@Repository

public interface FreelancerRepository extends JpaRepository<FreelancerEntity, Integer> {
<<<<<<< HEAD
	
=======
>>>>>>> af417413e6e55eb55ff2ca4fcd2e37dbc719e250
	Optional<FreelancerEntity> findByMember(MemberEntity member);
}
