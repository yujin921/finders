package net.datasa.finders.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import net.datasa.finders.domain.entity.ClientFieldEntity;
import net.datasa.finders.domain.entity.MemberEntity;

public interface ClientFieldRepository extends JpaRepository<ClientFieldEntity, Integer> {

	void deleteByClientId(MemberEntity client);

}
