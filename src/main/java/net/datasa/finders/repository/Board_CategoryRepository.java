package net.datasa.finders.repository;

import net.datasa.finders.domain.entity.BoardEntity;
import net.datasa.finders.domain.entity.Board_CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 게시판 관련 repository
 */

@Repository
public interface Board_CategoryRepository extends JpaRepository<Board_CategoryEntity, Integer> {
    List<Board_CategoryEntity> findByBoardEntity(BoardEntity entity);
}
