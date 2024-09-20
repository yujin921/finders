package net.datasa.finders.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import net.datasa.finders.domain.entity.ClientCategoryEntity;
import net.datasa.finders.domain.entity.MemberEntity;

public interface ClientCategoryRepository extends JpaRepository<ClientCategoryEntity, Integer> {

	void deleteByClientId(MemberEntity client);

}
