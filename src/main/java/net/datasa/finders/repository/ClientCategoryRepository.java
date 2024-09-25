package net.datasa.finders.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.datasa.finders.domain.entity.ClientCategoryEntity;
import net.datasa.finders.domain.entity.ClientFieldEntity;
import net.datasa.finders.domain.entity.MemberEntity;

public interface ClientCategoryRepository extends JpaRepository<ClientCategoryEntity, Integer> {

	void deleteByClientId(MemberEntity client);

	Optional<ClientCategoryEntity> findByClientIdAndCategoryText(MemberEntity memberEntity, String area);

}
