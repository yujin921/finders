package net.datasa.finders.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.FreelancerReviewDTO;
import net.datasa.finders.domain.entity.ClientReviewsEntity;
import net.datasa.finders.domain.entity.FreelancerReviewItemEntity;
import net.datasa.finders.domain.entity.FreelancerReviewsEntity;
import net.datasa.finders.repository.FreelancerReviewItemRepository;
import net.datasa.finders.repository.FreelancerReviewsRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class FreelancerReviewService {

    private final FreelancerReviewsRepository freelancerReviewsRepository;
    private final FreelancerReviewItemRepository freelancerReviewItemRepository;

    @Transactional
    public void saveFreelancerReview(FreelancerReviewDTO freelancerReviewDTO) {
        
        // 리뷰가 이미 작성되었는지 확인: 프로젝트 번호, 보내는 사람, 받는 사람 기준
        boolean reviewExists = freelancerReviewsRepository.existsByProjectNumAndSendIdAndReceivedId(
                freelancerReviewDTO.getProjectNum(), 
                freelancerReviewDTO.getSendId(),
                freelancerReviewDTO.getParticipantId()
        );

        if (reviewExists) {
            throw new IllegalStateException("해당 프로젝트에서 이 사용자에게 이미 리뷰가 작성되었습니다.");
        }

        // 프리랜서 리뷰 저장
        FreelancerReviewsEntity reviewEntity = FreelancerReviewsEntity.builder()
            .projectNum(freelancerReviewDTO.getProjectNum())
            .sendId(freelancerReviewDTO.getSendId())
            .receivedId(freelancerReviewDTO.getParticipantId())
            .rating(freelancerReviewDTO.getRating())
            .comment(freelancerReviewDTO.getComment())
            .reviewDate(LocalDateTime.now())
            .build();

        freelancerReviewsRepository.save(reviewEntity);

        // 리뷰 아이템 저장
        List<FreelancerReviewItemEntity> reviewItems = freelancerReviewDTO.getReviewItems().stream()
            .map(item -> FreelancerReviewItemEntity.builder()
                .freelancerReview(reviewEntity)  // 리뷰 엔터티와 연관
                .itemName(item.getItemName())
                .itemValue(item.isSelected())
                .build())
            .collect(Collectors.toList());

        freelancerReviewItemRepository.saveAll(reviewItems);
    }

    public List<FreelancerReviewsEntity> getFreelancerReviewsByFreelancerId(String freelancerId) {
        // 클라이언트 ID를 기준으로 해당 클라이언트에 대한 모든 프로젝트 리뷰를 조회
        return freelancerReviewsRepository.findByreceivedId(freelancerId);
        }
	
    @Autowired
    private FreelancerReviewsRepository freelancerReviewRepository;

    public List<FreelancerReviewDTO> getLatest20FreelancerReviews() {
        // Repository를 통해 최신 20개의 프리랜서 리뷰를 가져오는 로직
        List<FreelancerReviewsEntity> freelancerReviews = freelancerReviewRepository.findTop20ByOrderByReviewDateDesc();
        return freelancerReviews.stream()
                .map(review -> new FreelancerReviewDTO(review))
                .collect(Collectors.toList());
    }
}
