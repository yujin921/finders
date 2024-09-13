package net.datasa.finders.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import net.datasa.finders.domain.dto.FreelancerDataDTO;
import net.datasa.finders.domain.dto.FreelancerReviewDTO;
import net.datasa.finders.domain.dto.ReviewItemDTO;
import net.datasa.finders.domain.entity.FreelancerReviewItemEntity;
import net.datasa.finders.domain.entity.FreelancerReviewsEntity;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.repository.FreelancerReviewItemRepository;
import net.datasa.finders.repository.FreelancerReviewsRepository;
import net.datasa.finders.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class FreelancerReviewService {

    private final FreelancerReviewsRepository freelancerReviewsRepository;
    private final FreelancerReviewItemRepository freelancerReviewItemRepository;
    private final MemberRepository memberRepository;

    
    @Transactional
    public FreelancerReviewDTO createFreelancerReview(FreelancerReviewDTO reviewDTO) {
        // 리뷰 생성
        FreelancerReviewsEntity savedReviewEntity = saveReviewEntity(reviewDTO);

        // 평가 항목 저장
        saveReviewItems(reviewDTO.getReviewItems(), savedReviewEntity);

        // 저장된 데이터를 DTO로 변환하여 반환
        return FreelancerReviewDTO.builder()
                .reviewId(savedReviewEntity.getReviewId())
                .projectNum(savedReviewEntity.getProjectNum())
                .clientId(savedReviewEntity.getClientId())
                .freelancerId(savedReviewEntity.getFreelancerId())
                .rating(savedReviewEntity.getRating())
                .comment(savedReviewEntity.getComment())
                .reviewItems(reviewDTO.getReviewItems()) // 항목 리스트 그대로 사용
                .build();
    }

    private FreelancerReviewsEntity saveReviewEntity(FreelancerReviewDTO reviewDTO) {
        // 리뷰를 저장하는 메서드
        return freelancerReviewsRepository.save(
                FreelancerReviewsEntity.builder()
                        .projectNum(reviewDTO.getProjectNum())
                        .clientId(reviewDTO.getClientId())
                        .freelancerId(reviewDTO.getFreelancerId())
                        .rating(reviewDTO.getRating())
                        .comment(reviewDTO.getComment())
                        .reviewDate(LocalDateTime.now())
                        .build()
        );
    }

    private void saveReviewItems(List<ReviewItemDTO> reviewItems, FreelancerReviewsEntity reviewEntity) {
        // 리뷰 항목을 저장하는 메서드
        List<FreelancerReviewItemEntity> items = reviewItems.stream()
                .map(item -> FreelancerReviewItemEntity.builder()
                        .freelancerReview(reviewEntity)
                        .itemName(item.getItemName())
                        .itemValue(item.isSelected())
                        .build())
                .collect(Collectors.toList());

        freelancerReviewItemRepository.saveAll(items);
    }
    
 // getReviewData 메서드 예시
    public FreelancerReviewDTO getReviewData(int projectNum, String clientId) {
        // 프로젝트 번호와 클라이언트 ID를 받아서, 해당 프로젝트의 프리랜서 목록을 가져온다.
        List<MemberEntity> freelancers = memberRepository.findByMemberIdAndProjects_ProjectNum(clientId, projectNum);

        // 프리랜서 데이터를 FreelancerDataDTO로 변환하여 리스트 생성
        List<FreelancerDataDTO> freelancerData = freelancers.stream()
            .map(member -> new FreelancerDataDTO(member.getMemberId(), member.getMemberName()))
            .collect(Collectors.toList());

        // 필요한 초기 데이터 세팅하여 반환
        return FreelancerReviewDTO.builder()
            .projectNum(projectNum)
            .clientId(clientId)
            .freelancerData(freelancerData) // 프리랜서 리스트를 DTO에 추가
            .build();
    }
}
