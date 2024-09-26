package net.datasa.finders.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.ClientDataDTO;
import net.datasa.finders.domain.dto.FreelancerDataDTO;
import net.datasa.finders.domain.dto.FreelancerReviewDTO;
import net.datasa.finders.domain.dto.ReviewItemDTO;
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
    public void createFreelancerReview(FreelancerReviewDTO reviewDTO) {
        boolean isReviewCompleted = reviewRepository.existsByProjectNumAndClientIdAndFreelancerId(
                reviewDTO.getProjectNum(), reviewDTO.getClientId(), reviewDTO.getFreelancerId());

        if (isReviewCompleted) {
            throw new IllegalStateException("이미 이 프리랜서에 대한 리뷰가 작성되었습니다.");
        }

        FreelancerReviewsEntity savedReviewEntity = saveReviewEntity(reviewDTO);
        saveReviewItems(reviewDTO.getReviewItems(), savedReviewEntity);

        FreelancerReviewDTO.builder()
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

    // 역할에 따라 참가자 목록을 필터링하는 메서드
    @Transactional(readOnly = true)
    public List<ClientDataDTO> getParticipantsByRole(int projectNum, String userId, String role) {
        // 팀 멤버 전체 목록 가져오기
        List<MemberEntity> teamMembers = memberRepository.findByProjects_ProjectNum(projectNum);

        // 로그인한 사용자를 제외하고 역할에 맞게 필터링
        return teamMembers.stream()
                .filter(member -> !member.getMemberId().equals(userId))  // 로그인한 사용자는 제외
                .filter(member -> {
                    if ("freelancer".equals(role)) {
                        // 프리랜서를 선택하면 프리랜서만 보여줌
                        return member.getRoleName() == RoleName.ROLE_FREELANCER;
                    } else if ("client".equals(role)) {
                        // 클라이언트를 선택하면 클라이언트만 보여줌
                        return member.getRoleName() == RoleName.ROLE_CLIENT;
                    }
                    return false;
                })
                .map(member -> new ClientDataDTO(member.getMemberId(), member.getMemberName(), false))  // DTO로 변환
                .collect(Collectors.toList());
    }


    // 리뷰 작성 여부 확인
    @Transactional(readOnly = true)
    public boolean isReviewCompleted(String participantId, int projectNum, String userId) {
        return clientReviewRepository.existsByProjectNumAndClientIdAndFreelancerId(projectNum, userId, participantId);
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
}
