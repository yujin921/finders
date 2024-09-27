package net.datasa.finders.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "client_review_item")
public class ClientReviewItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private int itemId;

    @ManyToOne(fetch = FetchType.LAZY)  // 연관 관계 설정
    @JoinColumn(name = "review_id")  // 외래 키 설정
    private ClientReviewsEntity clientReview;  // ClientReviewsEntity와의 연관 관계 설정

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "item_value", nullable = false)
    private boolean itemValue;
}
