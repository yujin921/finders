package net.datasa.finders.repository;

import net.datasa.finders.domain.entity.ClientReviewsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientReviewsRepository extends JpaRepository<ClientReviewsEntity, Integer> {
    List<ClientReviewsEntity> findBySendId(String sendId);  // 클라이언트가 보낸 리뷰 조회

    boolean existsByProjectNumAndSendIdAndReceivedId(int projectNum, String sendId, String receivedId);

    boolean existsByProjectNumAndReceivedIdAndSendId(int projectNum, String receivedId, String sendId);

    // 클라이언트 ID로 리뷰 조회
    List<ClientReviewsEntity> findByReceivedId(String clientId);
    
    
    @Query("SELECT AVG(c.rating) FROM ClientReviewsEntity c WHERE c.receivedId = :clientId")
    Optional<Float> findAverageRatingByReceivedId(@Param("clientId") String clientId);
    
}
