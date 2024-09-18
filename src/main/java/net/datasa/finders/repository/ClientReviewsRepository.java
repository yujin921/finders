package net.datasa.finders.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.datasa.finders.domain.entity.ClientReviewsEntity; // 클라이언트 리뷰 엔티티

public interface ClientReviewsRepository extends JpaRepository<ClientReviewsEntity, Integer> {

    // 프로젝트 번호, 클라이언트 ID, 프리랜서 ID로 클라이언트 리뷰 조회
    @Query("SELECT cr FROM ClientReviewsEntity cr WHERE cr.projectNum = :projectNum AND cr.clientId = :clientId AND cr.freelancerId = :freelancerId")
    List<ClientReviewsEntity> findByProjectNumAndClientIdAndFreelancerId(
        @Param("projectNum") int projectNum,
        @Param("clientId") String clientId,
        @Param("freelancerId") String freelancerId);
    
    // 프리랜서 ID로 클라이언트 리뷰 조회 (변경됨)
    @Query("SELECT cr FROM ClientReviewsEntity cr WHERE cr.freelancerId = :freelancerId")
    List<ClientReviewsEntity> findByFreelancerId(@Param("freelancerId") String freelancerId);

    // 클라이언트 ID로 클라이언트 리뷰 조회
    @Query("SELECT cr FROM ClientReviewsEntity cr WHERE cr.clientId = :clientId")
    List<ClientReviewsEntity> findByClientId(@Param("clientId") String clientId);
    

    // 프로젝트 번호, 클라이언트 ID, 프리랜서 ID로 리뷰가 존재하는지 확인하는 메서드 추가
    boolean existsByProjectNumAndClientIdAndFreelancerId(int projectNum, String clientId, String freelancerId);
}
