package net.datasa.finders.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "client_review_item")
public class ClientReviewItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id") // 평가 항목의 고유 ID
    private int itemId;

    @ManyToOne
    @JoinColumn(name = "review_id") // 해당 항목이 속한 클라이언트 아이디
    private ClientReviewsEntity clientReview;

    @Column(name = "item_name", nullable = false) // 평가 항목 이름
    private String itemName;

    @Column(name = "item_value", nullable = false) // 해당 항목이 선택되었는지 여부 (TRUE/FALSE)
    private boolean itemValue;
}