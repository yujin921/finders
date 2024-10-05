package net.datasa.finders.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.ClientDataDTO;
import net.datasa.finders.domain.dto.ClientReviewDTO;
import net.datasa.finders.domain.dto.FreelancerReviewDTO;
import net.datasa.finders.domain.dto.UnifiedReviewDTO;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.domain.entity.RoleName;
import net.datasa.finders.repository.MemberRepository;
import net.datasa.finders.repository.ProjectPublishingRepository;
import net.datasa.finders.service.ClientReviewService;
import net.datasa.finders.service.FreelancerReviewService;
import net.datasa.finders.service.MemberService;
import net.datasa.finders.service.ReviewService;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("unifiedreview")
public class UnifiedReviewController {

    private final ReviewService reviewService;
    private final FreelancerReviewService freelancerReviewService;
    private final ClientReviewService clientReviewService;
    private final ProjectPublishingRepository projectRepository;
    private final MemberRepository memberRepository;
    private final MemberService memberService;

 
    @GetMapping("/writereview")
    public String getWriteReviewPage(
            @RequestParam("projectNum") int projectNum,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        String userId = userDetails.getUsername();
        
        // 사용자 정보 확인
        MemberEntity member = memberRepository.findByMemberId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        
        // 사용자 역할 가져오기
        RoleName userRole = member.getRoleName();  // ROLE_CLIENT 또는 ROLE_FREELANCER
        
        // 모델에 사용자 ID, 프로젝트 번호, 사용자 역할 추가
        model.addAttribute("userId", userId);
        model.addAttribute("projectNum", projectNum);
        model.addAttribute("userRole", userRole.name());  // 역할을 문자열로 넘김
        
        return "review/unifiedreview";  // 템플릿 경로
    }
    
    
    @GetMapping("/getParticipants")
    public ResponseEntity<List<ClientDataDTO>> getParticipants(
            @RequestParam("projectNum") int projectNum,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("role") String role) {

        String userId = userDetails.getUsername();  // 로그인된 사용자 ID

        try {
            List<ClientDataDTO> participants = reviewService.getParticipantsByRole(projectNum, userId, role);
            return ResponseEntity.ok(participants);
        } catch (Exception e) {
            log.error("참가자 목록 오류: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    
    
    
    
    
    @PostMapping("/submitClientReview")
    public ResponseEntity<String> submitClientReview(
            @RequestBody ClientReviewDTO reviewDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String clientId = userDetails.getUsername();
            reviewDTO.setSendId(clientId);  // 현재 로그인한 사용자 ID 설정

            clientReviewService.saveClientReview(reviewDTO);
            return ResponseEntity.ok("클라이언트 리뷰가 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("리뷰 저장 실패");
        }
    }

    @PostMapping("/submitFreelancerReview")
    public ResponseEntity<String> submitFreelancerReview(
            @RequestBody FreelancerReviewDTO reviewDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
        	log.info("Submitting freelancer review for projectNum: {}", reviewDTO.getProjectNum());
            String freelancerId = userDetails.getUsername();
            reviewDTO.setSendId(freelancerId);  // 현재 로그인한 사용자 ID 설정

            freelancerReviewService.saveFreelancerReview(reviewDTO);
            return ResponseEntity.ok("프리랜서 리뷰가 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("리뷰 저장 실패");
        }
    }

    
    
  
    // 프로젝트 존재 여부 확인
    private boolean projectExists(int projectNum) {
        return projectRepository.existsById(projectNum);
    }

    @GetMapping("/latest")
    @ResponseBody
    public List<UnifiedReviewDTO> getLatestReviews() {
        List<UnifiedReviewDTO> combinedReviews = new ArrayList<>();
        
        // 클라이언트 리뷰 추가
        List<ClientReviewDTO> clientReviews = clientReviewService.getLatest20ClientReviews();
        for (ClientReviewDTO clientReview : clientReviews) {
            // 로그로 작성자와 수신자 정보를 출력
            System.out.println("Client Review - SendId: " + clientReview.getSendId() + ", ReceivedId: " + clientReview.getParticipantId());
            
            UnifiedReviewDTO reviewDTO = new UnifiedReviewDTO();
            reviewDTO.setComment(clientReview.getComment());
            reviewDTO.setRating(clientReview.getRating());
            reviewDTO.setReviewerId(clientReview.getSendId()); // 작성자 ID
            reviewDTO.setReviewDate(clientReview.getReviewDate()); // 날짜만 추출
            reviewDTO.setRole("클라이언트");
            reviewDTO.setReceivedId(clientReview.getParticipantId()); // 수신자 ID
            
            // 수신자의 프로필 이미지 조회
            String recipientProfileImage = memberService.getProfileImageById(clientReview.getParticipantId());
            reviewDTO.setProfileImg(recipientProfileImage);

            combinedReviews.add(reviewDTO);
        }

        // 프리랜서 리뷰 추가
        List<FreelancerReviewDTO> freelancerReviews = freelancerReviewService.getLatest20FreelancerReviews();
        for (FreelancerReviewDTO freelancerReview : freelancerReviews) {
            // 로그로 작성자와 수신자 정보를 출력
            System.out.println("Freelancer Review - SendId: " + freelancerReview.getSendId() + ", ReceivedId: " + freelancerReview.getParticipantId());

            UnifiedReviewDTO reviewDTO = new UnifiedReviewDTO();
            reviewDTO.setComment(freelancerReview.getComment());
            reviewDTO.setRating(freelancerReview.getRating());
            reviewDTO.setReviewerId(freelancerReview.getSendId()); // 작성자 ID
            reviewDTO.setReviewDate(freelancerReview.getReviewDate()); // 날짜만 추출
            reviewDTO.setRole("프리랜서");
            reviewDTO.setReceivedId(freelancerReview.getParticipantId()); // 수신자 ID
            
            // 수신자의 프로필 이미지 조회
            String recipientProfileImage = memberService.getProfileImageById(freelancerReview.getParticipantId());
            reviewDTO.setProfileImg(recipientProfileImage);

            combinedReviews.add(reviewDTO);
        }

        return combinedReviews;
    }

}
