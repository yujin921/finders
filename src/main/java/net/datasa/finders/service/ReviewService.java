package net.datasa.finders.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.ClientDataDTO;
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
               .map(member -> {
                   // 리뷰 작성 여부 확인 (role에 따라 프리랜서 또는 클라이언트 리뷰 여부를 확인)
                   boolean reviewCompleted = checkIfReviewCompleted(projectNum, member.getMemberId(), userId, role);
                   return new ClientDataDTO(member.getMemberId(), member.getMemberName(), reviewCompleted);  // DTO로 변환
               })
               .collect(Collectors.toList());
   }

		// 리뷰가 완료되었는지 확인하는 메서드
		private boolean checkIfReviewCompleted(int projectNum, String participantId, String reviewerId, String role) {
		    // 역할에 따라 프리랜서와 클라이언트 리뷰 체크
		    if ("freelancer".equals(role)) {
		        // 프리랜서 리뷰에서 프로젝트 번호, 리뷰 대상(participantId), 작성자(reviewerId)로 리뷰 존재 확인
		        return reviewRepository.existsByProjectNumAndReceivedIdAndSendId(projectNum, participantId, reviewerId);
		    } else if ("client".equals(role)) {
		        // 클라이언트 리뷰에서 프로젝트 번호, 리뷰 대상(participantId), 작성자(reviewerId)로 리뷰 존재 확인
		        return clientReviewRepository.existsByProjectNumAndReceivedIdAndSendId(projectNum, participantId, reviewerId);
		    }
		    return false;
		}
}