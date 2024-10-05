package net.datasa.finders.domain.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.datasa.finders.domain.entity.ClientReviewsEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientReviewDTO {
	private int reviewId;
    private int projectNum;
    private String sendId; // 평가를 남긴 사용자
    private String participantId; // 평가받는 사용자
    private float rating;
    private String comment;
    private LocalDateTime reviewDate;
    private List<ReviewItemDTO> reviewItems;
    
    public ClientReviewDTO(ClientReviewsEntity entity) {
        this.reviewId = entity.getReviewId();
        this.projectNum = entity.getProjectNum();
        this.sendId = entity.getSendId();
        this.participantId = entity.getReceivedId();
        this.rating = entity.getRating();
        this.comment = entity.getComment();
        this.reviewDate = entity.getReviewDate();
    }

}
