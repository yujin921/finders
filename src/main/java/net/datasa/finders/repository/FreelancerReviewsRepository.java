package net.datasa.finders.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.datasa.finders.domain.entity.FreelancerReviewsEntity;

public interface FreelancerReviewsRepository extends JpaRepository<FreelancerReviewsEntity,Integer>{

    // 기존에 Optional로 되어 있는 메서드를 List로 수정합니다.
    @Query("SELECT fr FROM FreelancerReviewsEntity fr WHERE fr.projectNum = :projectNum AND fr.clientId = :clientId AND fr.freelancerId = :freelancerId")
    List<FreelancerReviewsEntity> findByProjectNumAndClientIdAndFreelancerId(
        @Param("projectNum") int projectNum,
        @Param("clientId") String clientId,
        @Param("freelancerId") String freelancerId); 
    
    // 프로젝트 번호와 클라이언트 ID, 프리랜서 ID로 리뷰가 존재하는지 확인하는 메서드
    boolean existsByProjectNumAndClientIdAndFreelancerId(int projectNum, String clientId, String freelancerId);

    // 프로젝트 번호와 프리랜서 ID로 리뷰가 존재하는지 확인하는 메서드 추가
    boolean existsByProjectNumAndFreelancerId(int projectNum, String freelancerId);

    // 프리랜서가 받은 모든 리뷰 조회
    List<FreelancerReviewsEntity> findByFreelancerId(String freelancerId);
    
    // 프리랜서가 남긴 리뷰 조회
    List<FreelancerReviewsEntity> findByClientId(String freelancerId);

    // 프로젝트 번호에 대한 평균 평점 조회
    @Query("SELECT AVG(f.rating) FROM FreelancerReviewsEntity f WHERE f.projectNum = :projectNum")
    Optional<Float> findAverageRatingByProjectNum(@Param("projectNum") int projectNum);


}
