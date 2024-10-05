package net.datasa.finders.domain.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.datasa.finders.domain.entity.FreelancerReviewsEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FreelancerReviewDTO {
		private int reviewId;
		private int projectNum;
	    private String sendId; // 평가를 남긴 사용자
	    private String participantId; // 평가받는 사용자
	    private float rating;
	    private String comment;
	    private LocalDateTime reviewDate;
	    private List<ReviewItemDTO> reviewItems;
	    
	    // FreelancerReviewsEntity를 매개변수로 받는 생성자 추가
	    public FreelancerReviewDTO(FreelancerReviewsEntity entity) {
	        this.reviewId = entity.getReviewId();
	        this.projectNum = entity.getProjectNum();
	        this.sendId = entity.getSendId();
	        this.participantId = entity.getReceivedId();
	        this.rating = entity.getRating();
	        this.comment = entity.getComment();
	        this.reviewDate = entity.getReviewDate();
	    }

}
