package net.datasa.finders.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import net.datasa.finders.domain.dto.ClientDataDTO;
import net.datasa.finders.domain.dto.ClientReviewDTO;
import net.datasa.finders.domain.dto.FreelancerDataDTO;
import net.datasa.finders.domain.dto.FreelancerReviewDTO;
import net.datasa.finders.domain.dto.ReviewItemDTO;
import net.datasa.finders.domain.entity.ClientReviewsEntity;
import net.datasa.finders.domain.entity.FreelancerReviewItemEntity;
import net.datasa.finders.domain.entity.FreelancerReviewsEntity;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.domain.entity.RoleName;
import net.datasa.finders.repository.ClientReviewsRepository;
import net.datasa.finders.repository.FreelancerReviewsRepository;
import net.datasa.finders.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class FreelancerReviewService {
    
    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(FreelancerReviewService.class);
    
    private final MemberRepository memberRepository;
    private final FreelancerReviewsRepository freelancerReviewRepository;
    private final ClientReviewsRepository clientReviewRepository;

    /**
     * 특정 프로젝트에 참여 중인 프리랜서 목록을 조회합니다.
     * @param projectNum 프로젝트 번호
     * @param clientId 클라이언트 ID
     * @return 프리랜서 목록
     */
    @Transactional(readOnly = true)
    public List<ClientDataDTO> getFreelancersByProject(int projectNum, String clientId) {
        List<MemberEntity> teamMembers = memberRepository.findByProjects_ProjectNum(projectNum);

        return teamMembers.stream()
                .filter(member -> !member.getMemberId().equals(clientId)) // 클라이언트 본인 제외
                .filter(member -> member.getRoleName() == RoleName.ROLE_FREELANCER) // 프리랜서만 포함
                .map(member -> {
                    boolean isReviewCompleted = freelancerReviewRepository.existsByProjectNumAndFreelancerId(
                            projectNum, member.getMemberId());
                    return new ClientDataDTO(member.getMemberId(), member.getMemberName(), isReviewCompleted);
                })
                .collect(Collectors.toList());
    }

    // 프리랜서가 받은 리뷰 목록을 가져오는 메서드
    @Transactional(readOnly = true)
    public List<FreelancerReviewDTO> getReceivedReviews(String freelancerId) {
        return freelancerReviewRepository.findByFreelancerId(freelancerId).stream()
                .map(this::convertToFreelancerReviewDTO)
                .collect(Collectors.toList());
    }

    // 프리랜서 리뷰를 DTO로 변환하는 메서드
    private FreelancerReviewDTO convertToFreelancerReviewDTO(FreelancerReviewsEntity entity) {
        return FreelancerReviewDTO.builder()
                .reviewId(entity.getReviewId())
                .projectNum(entity.getProjectNum())
                .clientId(entity.getClientId())
                .freelancerId(entity.getFreelancerId())
                .rating(entity.getRating())
                .comment(entity.getComment())
                .reviewDate(entity.getReviewDate())
                .reviewItems(entity.getReviewItems().stream()
                        .map(this::convertReviewItemToDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    // 리뷰 항목을 DTO로 변환하는 메서드
    private ReviewItemDTO convertReviewItemToDTO(FreelancerReviewItemEntity entity) {
        return ReviewItemDTO.builder()
                .itemName(entity.getItemName())
                .selected(entity.isItemValue())
                .build();
    }

    // 프리랜서 리뷰 저장 메서드
    @Transactional
    public void createFreelancerReview(FreelancerReviewDTO reviewDTO) {
        FreelancerReviewsEntity reviewEntity = FreelancerReviewsEntity.builder()
                .projectNum(reviewDTO.getProjectNum())
                .clientId(reviewDTO.getClientId())
                .freelancerId(reviewDTO.getFreelancerId())
                .rating(reviewDTO.getRating())
                .comment(reviewDTO.getComment())
                .reviewDate(LocalDateTime.now())
                .build();

        // 리뷰 항목 저장
        List<FreelancerReviewItemEntity> reviewItems = reviewDTO.getReviewItems().stream()
                .map(item -> FreelancerReviewItemEntity.builder()
                        .freelancerReview(reviewEntity)
                        .itemName(item.getItemName())
                        .itemValue(item.isSelected())
                        .build())
                .collect(Collectors.toList());

        reviewEntity.setReviewItems(reviewItems);
        freelancerReviewRepository.save(reviewEntity);
    }

    // 클라이언트와 프리랜서가 남긴 리뷰를 모두 가져오는 메서드
    @Transactional(readOnly = true)
    public List<Object> getAllReviewsForFreelancer(String freelancerId) {
        List<Object> combinedReviews = new ArrayList<>();
        
        // 클라이언트가 프리랜서에게 남긴 리뷰
        List<ClientReviewsEntity> clientReviews = clientReviewRepository.findByFreelancerId(freelancerId);
        combinedReviews.addAll(clientReviews.stream().map(this::convertClientReviewToDTO).collect(Collectors.toList()));

        // 프리랜서가 프리랜서에게 남긴 리뷰
        List<ClientReviewsEntity> freelancerToFreelancerReviews = clientReviewRepository.findByClientId(freelancerId);
        combinedReviews.addAll(freelancerToFreelancerReviews.stream().map(this::convertClientReviewToDTO).collect(Collectors.toList()));

        // 프리랜서가 받은 리뷰
        List<FreelancerReviewsEntity> freelancerReviews = freelancerReviewRepository.findByFreelancerId(freelancerId);
        combinedReviews.addAll(freelancerReviews.stream().map(this::convertToFreelancerReviewDTO).collect(Collectors.toList()));

        return combinedReviews;
    }

    // 클라이언트 리뷰를 DTO로 변환하는 메서드
    private ClientReviewDTO convertClientReviewToDTO(ClientReviewsEntity entity) {
        return ClientReviewDTO.builder()
                .reviewId(entity.getReviewId())
                .projectNum(entity.getProjectNum())
                .clientId(entity.getFreelancerId())
                .freelancerId(entity.getClientId())
                .rating(entity.getRating())
                .comment(entity.getComment())
                .reviewDate(entity.getReviewDate())
                .build();
    }
}
