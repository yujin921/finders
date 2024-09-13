package net.datasa.finders.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import net.datasa.finders.domain.entity.FreelancerReviewItemEntity;

public interface FreelancerReviewItemRepository extends JpaRepository<FreelancerReviewItemEntity,Integer>{
	   // 리뷰 ID를 기준으로 평가 항목을 찾는 메서드 추가
    List<FreelancerReviewItemEntity> findByFreelancerReview_ReviewId(int reviewId);
}
