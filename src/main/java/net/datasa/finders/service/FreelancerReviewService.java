package net.datasa.finders.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import net.datasa.finders.domain.dto.FreelancerDataDTO;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.domain.entity.RoleName;
import net.datasa.finders.repository.FreelancerReviewsRepository;
import net.datasa.finders.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class FreelancerReviewService {

    private static final Logger logger = LoggerFactory.getLogger(FreelancerReviewService.class);
    private final MemberRepository memberRepository;
    private final FreelancerReviewsRepository reviewRepository;  // 추가된 부분

    /**
     * 특정 프로젝트에 참여 중인 프리랜서 목록을 조회합니다.
     * @param projectNum 프로젝트 번호
     * @param clientId 클라이언트 ID
     * @return 프리랜서 목록
     */
    @Transactional(readOnly = true)
    public List<FreelancerDataDTO> getFreelancersByProject(int projectNum, String clientId) {
        // 프로젝트 번호로 팀 참가자 가져오기
        List<MemberEntity> teamMembers = memberRepository.findByProjects_ProjectNum(projectNum);

        // 프리랜서 목록 생성 시 리뷰 작성 여부를 확인하여 DTO에 반영
        return teamMembers.stream()
                .filter(member -> !member.getMemberId().equals(clientId)) // 클라이언트 본인 제외
                .filter(member -> member.getRoleName() == RoleName.ROLE_FREELANCER) // 프리랜서만 포함
                .map(member -> {
                    boolean isReviewCompleted = reviewRepository.existsByProjectNumAndClientIdAndFreelancerId(
                            projectNum, clientId, member.getMemberId());
                    return new FreelancerDataDTO(member.getMemberId(), member.getMemberName(), isReviewCompleted);
                })
                .collect(Collectors.toList());
    }

    /**
     * 프리랜서의 리뷰 점수를 계산하는 예시 메서드입니다.
     * @param freelancerId 프리랜서 ID
     * @return 리뷰 점수 계산 결과
     */
    @Transactional(readOnly = true)
    public double calculateFreelancerRating(String freelancerId) {
        // 리뷰 데이터를 활용하여 특정 프리랜서의 평균 점수를 계산하는 로직을 작성합니다.
        // 예를 들어, reviewRepository를 사용하여 리뷰 데이터를 가져오고 평균 점수를 계산합니다.
        // return calculatedRating;
        return 0.0; // 예시로 빈 값을 반환하고 실제 로직을 작성합니다.
    }
}
