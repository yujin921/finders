package net.datasa.finders.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.datasa.finders.domain.entity.FunctionTitleEntity;

@Repository
public interface FunctionTitleRepository extends JpaRepository<FunctionTitleEntity, Integer> {
	
}
