package net.datasa.finders.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.FreelancerDataDTO;
import net.datasa.finders.domain.dto.FreelancerReviewDTO;
import net.datasa.finders.domain.dto.ReviewItemDTO;
import net.datasa.finders.domain.dto.UnifiedReviewDTO;
import net.datasa.finders.domain.entity.ClientReviewsEntity;
import net.datasa.finders.domain.entity.FreelancerReviewItemEntity;
import net.datasa.finders.domain.entity.FreelancerReviewsEntity;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.domain.entity.RoleName;
import net.datasa.finders.repository.ClientReviewsRepository;
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
    private final ClientReviewsRepository clientReviewRepository;
    private final MemberRepository memberRepository;
    private final ProjectPublishingRepository projectPublishingRepository;

    @Transactional
    public FreelancerReviewDTO createFreelancerReview(FreelancerReviewDTO reviewDTO) {
        boolean isReviewCompleted = reviewRepository.existsByProjectNumAndClientIdAndFreelancerId(
                reviewDTO.getProjectNum(), reviewDTO.getClientId(), reviewDTO.getFreelancerId());

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
        List<MemberEntity> freelancers = memberRepository.findByProjects_ProjectNum(projectNum)
                .stream()
                .filter(member -> member.getRoleName() == RoleName.ROLE_FREELANCER)
                .collect(Collectors.toList());

        List<FreelancerDataDTO> freelancerDTOs = freelancers.stream()
            .map(freelancer -> {
                boolean isReviewCompleted = reviewRepository.existsByProjectNumAndClientIdAndFreelancerId(
                        projectNum, clientId, freelancer.getMemberId());

                log.info("Freelancer ID: {}, isReviewCompleted: {}", freelancer.getMemberId(), isReviewCompleted);

                return new FreelancerDataDTO(freelancer.getMemberId(), freelancer.getMemberName(), isReviewCompleted);
            })
            .collect(Collectors.toList());

        return FreelancerReviewDTO.builder()
            .projectNum(projectNum)
            .clientId(clientId)
            .freelancerData(freelancerDTOs)
            .build();
    }

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

    // 클라이언트와 프리랜서 리뷰를 모두 조회하여 통합 반환하는 메서드 추가
    @Transactional(readOnly = true)
    public List<UnifiedReviewDTO> getAllReviewsForFreelancer(String freelancerId) {
        List<ClientReviewsEntity> clientReviews = clientReviewRepository.findByFreelancerId(freelancerId);
        List<FreelancerReviewsEntity> freelancerReviews = reviewRepository.findByFreelancerId(freelancerId);

        List<UnifiedReviewDTO> unifiedReviews = new ArrayList<>();
        clientReviews.forEach(review -> unifiedReviews.add(UnifiedReviewDTO.fromClientReview(review)));
        freelancerReviews.forEach(review -> unifiedReviews.add(UnifiedReviewDTO.fromFreelancerReview(review)));

        return unifiedReviews;
    }
}
