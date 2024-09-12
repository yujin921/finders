package net.datasa.finders.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.datasa.finders.domain.entity.ClientEntity;
import net.datasa.finders.domain.entity.MemberEntity;

@Repository
public interface ClientRepository extends JpaRepository<ClientEntity, String> {
	
	Optional<ClientEntity> findByMember(MemberEntity member);

}
