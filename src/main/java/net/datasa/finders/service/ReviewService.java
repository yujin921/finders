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
import net.datasa.finders.domain.entity.RoleName;
import net.datasa.finders.repository.FreelancerReviewItemRepository;
import net.datasa.finders.repository.FreelancerReviewsRepository;
import net.datasa.finders.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final FreelancerReviewsRepository reviewRepository;
    private final FreelancerReviewItemRepository reviewItemRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public FreelancerReviewDTO createFreelancerReview(FreelancerReviewDTO reviewDTO) {
        FreelancerReviewsEntity savedReviewEntity = saveReviewEntity(reviewDTO);
        saveReviewItems(reviewDTO.getReviewItems(), savedReviewEntity);

        return convertToDTO(savedReviewEntity, reviewDTO.getReviewItems());
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

    private FreelancerReviewDTO convertToDTO(FreelancerReviewsEntity reviewEntity, List<ReviewItemDTO> reviewItems) {
        return FreelancerReviewDTO.builder()
                .reviewId(reviewEntity.getReviewId())
                .projectNum(reviewEntity.getProjectNum())
                .clientId(reviewEntity.getClientId())
                .freelancerId(reviewEntity.getFreelancerId())
                .rating(reviewEntity.getRating())
                .comment(reviewEntity.getComment())
                .reviewItems(reviewItems)
                .build();
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
        List<MemberEntity> freelancers = getTeamFreelancers(projectNum, clientId);

        List<FreelancerDataDTO> freelancerDTOs = freelancers.stream()
            .map(freelancer -> {
                boolean isReviewCompleted = reviewRepository.existsByProjectNumAndClientIdAndFreelancerId(
                        projectNum, clientId, freelancer.getMemberId());
                return new FreelancerDataDTO(freelancer.getMemberId(), freelancer.getMemberName(), isReviewCompleted);
            })
            .collect(Collectors.toList());

        return FreelancerReviewDTO.builder()
            .projectNum(projectNum)
            .clientId(clientId)
            .freelancerData(freelancerDTOs)
            .build();
    }
}
