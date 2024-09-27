package net.datasa.finders.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "freelancer_review_item")
public class FreelancerReviewItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private int itemId;

    @ManyToOne
    @JoinColumn(name = "review_id", nullable = false)  // 리뷰 엔터티와 연결
    private FreelancerReviewsEntity freelancerReview;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "item_value", nullable = false)
    private boolean itemValue;}