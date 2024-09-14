package net.datasa.finders.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
        // 프로젝트 번호와 클라이언트 ID를 기반으로 평가할 프리랜서 목록을 조회
        List<MemberEntity> freelancers = getTeamFreelancers(projectNum, clientId);

        // 프리랜서 데이터를 DTO로 변환하면서, 각 프로젝트별로 리뷰 완료 여부 확인
        List<FreelancerDataDTO> freelancerDTOs = freelancers.stream()
            .map(freelancer -> {
                // 프로젝트별로 리뷰 존재 여부 확인
                boolean isReviewCompleted = reviewRepository.existsByProjectNumAndClientIdAndFreelancerId(
                        projectNum, clientId, freelancer.getMemberId());

                // DTO 생성 시 작성 완료 상태 설정
                return new FreelancerDataDTO(freelancer.getMemberId(), freelancer.getMemberName(), isReviewCompleted);
            })
            .collect(Collectors.toList());

        // 예상되는 데이터 구조로 반환
        return FreelancerReviewDTO.builder()
            .projectNum(projectNum)
            .clientId(clientId)
            .freelancerData(freelancerDTOs) // 프리랜서 리스트를 DTO에 추가
            .build();
    }

}
