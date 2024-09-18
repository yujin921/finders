package net.datasa.finders.domain.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.datasa.finders.domain.entity.ClientReviewsEntity;
import net.datasa.finders.domain.entity.FreelancerReviewsEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedReviewDTO {
    private String reviewType; // "클라이언트 리뷰" 또는 "프리랜서 리뷰"
    private String reviewerId; // 리뷰를 남긴 사람의 ID
    private String receivedId; // 리뷰를 받은 사람의 ID
    private float rating;      // 평점
    private String comment;    // 코멘트
    private LocalDateTime reviewDate; // 리뷰 작성 시간
    private int projectNum;    // 프로젝트 번호
    private List<ReviewItemDTO> reviewItems; // 리뷰 항목 리스트 추가

    // 클라이언트 리뷰를 DTO로 변환하는 메서드
    public static UnifiedReviewDTO fromClientReview(ClientReviewsEntity clientReview) {
        return UnifiedReviewDTO.builder()
                .reviewType("클라이언트 리뷰")
                .reviewerId(clientReview.getFreelancerId())
                .receivedId(clientReview.getClientId())
                .rating(clientReview.getRating())
                .comment(clientReview.getComment())
                .reviewDate(clientReview.getReviewDate())
                .projectNum(clientReview.getProjectNum())
                .reviewItems(clientReview.getReviewItems().stream()
                    .map(item -> new ReviewItemDTO(item.getItemName(), item.isItemValue()))
                    .collect(Collectors.toList())) // 리뷰 항목 추가
                .build();
    }

    // 프리랜서 리뷰를 DTO로 변환하는 메서드
    public static UnifiedReviewDTO fromFreelancerReview(FreelancerReviewsEntity freelancerReview) {
        return UnifiedReviewDTO.builder()
                .reviewType("프리랜서 리뷰")
                .reviewerId(freelancerReview.getClientId())
                .receivedId(freelancerReview.getFreelancerId())
                .rating(freelancerReview.getRating())
                .comment(freelancerReview.getComment())
                .reviewDate(freelancerReview.getReviewDate())
                .projectNum(freelancerReview.getProjectNum())
                .reviewItems(freelancerReview.getReviewItems().stream()
                    .map(item -> new ReviewItemDTO(item.getItemName(), item.isItemValue()))
                    .collect(Collectors.toList())) // 리뷰 항목 추가
                .build();
    }
}
