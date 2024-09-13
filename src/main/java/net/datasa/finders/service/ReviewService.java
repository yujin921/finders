package net.datasa.finders.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class ReviewService {

    private final FreelancerReviewsRepository reviewRepository;
    private final FreelancerReviewItemRepository reviewItemRepository;
    private final MemberRepository memberRepository;

    public ReviewService(FreelancerReviewsRepository reviewRepository, 
    					 FreelancerReviewItemRepository reviewItemRepository,
    					 MemberRepository memberRepository) {
        this.reviewRepository = reviewRepository;
        this.reviewItemRepository = reviewItemRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public void saveReview(FreelancerReviewDTO reviewDTO) {
        // DTO에서 엔티티로 변환 및 저장
        FreelancerReviewsEntity reviewEntity = FreelancerReviewsEntity.builder()
                .projectNum(reviewDTO.getProjectNum())
                .clientId(reviewDTO.getClientId())
                .freelancerId(reviewDTO.getFreelancerId())
                .rating(reviewDTO.getRating())
                .comment(reviewDTO.getComment())
                .reviewDate(LocalDateTime.now())
                .build();

        reviewRepository.save(reviewEntity);

        // ReviewItem 저장
        for (ReviewItemDTO item : reviewDTO.getReviewItems()) {
            FreelancerReviewItemEntity itemEntity = FreelancerReviewItemEntity.builder()
                    .freelancerReview(reviewEntity)
                    .itemName(item.getItemName())
                    .itemValue(item.isSelected())
                    .build();
            reviewItemRepository.save(itemEntity);
        }
    }
    
    
 // 프리랜서만 필터링하는 메서드 수정
    public List<MemberEntity> getTeamFreelancers(int projectNum, String clientId) {
        // 프로젝트 번호로 팀 참가자 가져오기
        List<MemberEntity> teamMembers = memberRepository.findByProjectNum(projectNum);

        // 클라이언트 본인은 제외하고 프리랜서만 필터링
        return teamMembers.stream()
                .filter(member -> !member.getMemberId().equals(clientId))
                .filter(member -> member.getRoleName() == RoleName.ROLE_FREELANCER) // 프리랜서 필터링 조건
                .collect(Collectors.toList());
    }
    
    
    @Transactional(readOnly = true)
    public FreelancerReviewDTO getReviewData(int projectNum, String clientId) {
        // 프로젝트 번호와 클라이언트 ID를 기반으로 평가할 프리랜서 목록을 조회
        List<MemberEntity> freelancers = getTeamFreelancers(projectNum, clientId); // 정확한 데이터 조회 메서드 사용

        // 프리랜서 데이터를 DTO로 변환하여 프론트엔드에 전달
        List<FreelancerDataDTO> freelancerDTOs = freelancers.stream()
            .map(freelancer -> new FreelancerDataDTO(freelancer.getMemberId(), freelancer.getMemberName()))
            .collect(Collectors.toList());

        // 예상되는 데이터 구조로 반환
        return FreelancerReviewDTO.builder()
            .projectNum(projectNum)
            .clientId(clientId)
            .freelancerData(freelancerDTOs) // 프리랜서 리스트를 DTO에 추가
            .build();
    }
}
