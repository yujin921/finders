package net.datasa.finders.service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.FreelancerDataDTO;
import net.datasa.finders.domain.dto.FreelancerReviewDTO;
import net.datasa.finders.domain.dto.ReviewItemDTO;
import net.datasa.finders.domain.entity.FreelancerReviewItemEntity;
import net.datasa.finders.domain.entity.FreelancerReviewsEntity;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.domain.entity.RoleName;
import net.datasa.finders.repository.FreelancerReviewItemRepository;
import net.datasa.finders.repository.FreelancerReviewsRepository;
import net.datasa.finders.repository.MemberRepository;
import net.datasa.finders.repository.ProjectPublishingRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final FreelancerReviewsRepository reviewRepository;
    private final FreelancerReviewItemRepository reviewItemRepository;
    private final MemberRepository memberRepository;
    private final ProjectPublishingRepository projectPublishingRepository;
    

    @Transactional
    public FreelancerReviewDTO createFreelancerReview(FreelancerReviewDTO reviewDTO) {
        // 클라이언트 ID, 프로젝트 번호, 프리랜서 ID로 이미 작성된 리뷰가 있는지 확인
        boolean isReviewCompleted = reviewRepository.existsByProjectNumAndClientIdAndFreelancerId(
                reviewDTO.getProjectNum(), reviewDTO.getClientId(), reviewDTO.getFreelancerId());

        // 이미 리뷰가 작성되었다면 예외 발생
        if (isReviewCompleted) {
            throw new IllegalStateException("이미 이 프리랜서에 대한 리뷰가 작성되었습니다.");
        }

        FreelancerReviewsEntity savedReviewEntity = saveReviewEntity(reviewDTO);
        saveReviewItems(reviewDTO.getReviewItems(), savedReviewEntity);

        return FreelancerReviewDTO.builder()
                .reviewId(savedReviewEntity.getReviewId())
                .projectNum(savedReviewEntity.getProjectNum())
                .clientId(savedReviewEntity.getClientId())
                .freelancerId(savedReviewEntity.getFreelancerId())
                .rating(savedReviewEntity.getRating())
                .comment(savedReviewEntity.getComment())
                .reviewItems(reviewDTO.getReviewItems())
                .build();
    }
    
    // saveReviewEntity 메서드 추가
    private FreelancerReviewsEntity saveReviewEntity(FreelancerReviewDTO reviewDTO) {
        return reviewRepository.save(
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
        List<FreelancerReviewItemEntity> items = reviewItems.stream()
                .map(item -> FreelancerReviewItemEntity.builder()
                        .freelancerReview(reviewEntity)
                        .itemName(item.getItemName())
                        .itemValue(item.isSelected())
                        .build())
                .collect(Collectors.toList());

        reviewItemRepository.saveAll(items);
    }

    public List<MemberEntity> getTeamFreelancers(int projectNum, String clientId) {
        List<MemberEntity> teamMembers = memberRepository.findByProjects_ProjectNum(projectNum);

        return teamMembers.stream()
                .filter(member -> !member.getMemberId().equals(clientId))
                .filter(member -> member.getRoleName() == RoleName.ROLE_FREELANCER)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FreelancerReviewDTO getReviewData(int projectNum, String clientId) {
        // 프로젝트 번호와 클라이언트 ID를 기반으로 평가할 프리랜서 목록을 조회
        List<MemberEntity> freelancers = memberRepository.findByProjects_ProjectNum(projectNum)
                .stream()
                .filter(member -> member.getRoleName() == RoleName.ROLE_FREELANCER)
                .collect(Collectors.toList());

        // 프리랜서 데이터를 DTO로 변환하면서, 각 프로젝트별로 리뷰 완료 여부 확인
        List<FreelancerDataDTO> freelancerDTOs = freelancers.stream()
            .map(freelancer -> {
                // 특정 프로젝트와 프리랜서 ID로 리뷰 존재 여부 확인
                boolean isReviewCompleted = reviewRepository.existsByProjectNumAndClientIdAndFreelancerId(
                        projectNum, clientId, freelancer.getMemberId());

                // 로그 추가: 각 프리랜서의 작성 완료 상태 출력
                log.info("Freelancer ID: {}, isReviewCompleted: {}", freelancer.getMemberId(), isReviewCompleted);

                // DTO 생성 시 작성 완료 상태 설정
                return new FreelancerDataDTO(freelancer.getMemberId(), freelancer.getMemberName(), isReviewCompleted);
            })
            .collect(Collectors.toList());

        // 리뷰 데이터 반환
        return FreelancerReviewDTO.builder()
            .projectNum(projectNum)
            .clientId(clientId)
            .freelancerData(freelancerDTOs) // 프리랜서 리스트를 DTO에 추가
            .build();
    }

    // 평균 평점을 계산하는 메소드
    public double calculateAverageRating() {
        List<FreelancerReviewsEntity> reviews = reviewRepository.findAll();
        if (reviews.isEmpty()) {
            return 0.0;
        }
        double totalRating = 0;
        for (FreelancerReviewsEntity review : reviews) {
            totalRating += review.getRating();
        }
        return totalRating / reviews.size();
    }

}
