package net.datasa.finders.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import net.datasa.finders.domain.entity.ClientReviewItemEntity;
import net.datasa.finders.domain.entity.ClientReviewsEntity;

public interface ClientReviewItemRepository extends JpaRepository<ClientReviewItemEntity, Integer> {
}