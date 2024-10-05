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
@Table(name = "freelancer_reviews")
public class FreelancerReviewsEntity {

	  @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name = "review_id")
	    private int reviewId;

	    @Column(name = "project_num", nullable = false)
	    private int projectNum;

	    @Column(name = "send_id", nullable = false)
	    private String sendId;  // 리뷰 작성자 (프리랜서)

	    @Column(name = "received_id", nullable = false)
	    private String receivedId;  // 평가 대상 (클라이언트)

	    @Column(name = "rating", nullable = false)
	    private float rating;

	    @Column(name = "comment", nullable = false)
	    private String comment;

	    @Column(name = "review_date", nullable = false)
	    private LocalDateTime reviewDate;

	    
	    @OneToMany(mappedBy = "freelancerReview", cascade = CascadeType.ALL, orphanRemoval = true)
	    private List<FreelancerReviewItemEntity> reviewItems;  // 연결된 평가 항목들
}
