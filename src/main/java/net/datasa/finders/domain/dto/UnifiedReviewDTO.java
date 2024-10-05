package net.datasa.finders.domain.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.datasa.finders.domain.entity.ClientReviewsEntity;
import net.datasa.finders.domain.entity.FreelancerReviewsEntity;
import net.datasa.finders.repository.ClientReviewsRepository;

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
    
    private String role;
    private String profileImg;
    
}
