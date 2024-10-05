package net.datasa.finders.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.datasa.finders.domain.entity.ClientReviewsEntity;
import net.datasa.finders.domain.entity.FreelancerReviewsEntity;

public interface FreelancerReviewsRepository extends JpaRepository<FreelancerReviewsEntity,Integer>{
	// 프로젝트 번호와 받은 사람 ID로 해당 리뷰가 존재하는지 확인하는 메서드
    boolean existsByProjectNumAndReceivedIdAndSendId(int projectNum, String receivedId, String sendId);

    boolean existsByProjectNumAndSendIdAndReceivedId(int projectNum, String sendId, String receivedId);

	List<FreelancerReviewsEntity> findByreceivedId(String receivedId);

    List<FreelancerReviewsEntity> findTop20ByOrderByReviewDateDesc(); // 최신순 20개 조회

    
}
