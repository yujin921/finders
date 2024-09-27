package net.datasa.finders.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "client_reviews")
public class ClientReviewsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private int reviewId;

    @Column(name = "project_num", nullable = false)
    private int projectNum;

    @Column(name = "send_id", nullable = false)
    private String sendId;  // 리뷰 작성자 (클라이언트)

    @Column(name = "received_id", nullable = false)
    private String receivedId;  // 평가 대상 (프리랜서)

    @Column(name = "rating", nullable = false)
    private float rating;

    @Column(name = "comment", nullable = false)
    private String comment;

    @Column(name = "review_date", nullable = false)
    private LocalDateTime reviewDate;

    @OneToMany(mappedBy = "clientReview", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClientReviewItemEntity> reviewItems;  // 연결된 평가 항목들
}
