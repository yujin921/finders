package net.datasa.finders.domain.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "client_reviews")
public class ClientReviewsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id") // 리뷰 ID
    private int reviewId;

    @Column(name = "project_num", nullable = false) // 프로젝트 번호
    private int projectNum;

    @Column(name = "send_id", nullable = false, length = 20) // 평가를 남긴 클라이언트 ID
    private String freelancerId;

    @Column(name = "received_id", nullable = false, length = 20) // 평가를 받은 프리랜서 ID
    private String clientId;

    @Column(name = "rating", nullable = false) // 총 평점 (0.5~5점)
    private float rating;

    @Column(name = "comment") // 코멘트
    private String comment;

    @Column(name = "review_date", nullable = false) // 리뷰 작성 시간
    private LocalDateTime reviewDate;

    @OneToMany(mappedBy = "clientReview", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClientReviewItemEntity> reviewItems; // 선택된 평가 항목 리스트
}
