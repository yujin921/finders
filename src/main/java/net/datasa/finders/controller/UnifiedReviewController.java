package net.datasa.finders.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.ClientDataDTO;
import net.datasa.finders.domain.dto.ClientReviewDTO;
import net.datasa.finders.domain.dto.FreelancerReviewDTO;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.domain.entity.RoleName;
import net.datasa.finders.repository.MemberRepository;
import net.datasa.finders.repository.ProjectPublishingRepository;
import net.datasa.finders.service.ClientReviewService;
import net.datasa.finders.service.FreelancerReviewService;
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
    
    @GetMapping("/writereview")
    public String getWriteReviewPage(
            @RequestParam("projectNum") int projectNum,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        // 로그인된 사용자의 ID를 가져옴
        String userId = userDetails.getUsername(); 

        // 모델에 userId와 projectNum을 추가
        model.addAttribute("userId", userId);
        model.addAttribute("projectNum", projectNum);
        
        log.info("userid = {}, projectnum = {}", userId, projectNum);

        // 뷰 이름 반환
        return "review/unifiedreview"; // Thymeleaf 템플릿 경로
    }
    
    
    @GetMapping("/getParticipants")
    public ResponseEntity<List<ClientDataDTO>> getParticipants(
            @RequestParam("projectNum") int projectNum,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("role") String role) {

        String userId = userDetails.getUsername();  // 로그인된 사용자 ID
        log.info("getParticipants 호출됨: projectNum = {}, userId = {}, role = {}", projectNum, userId, role);

        try {
            List<ClientDataDTO> participants = reviewService.getParticipantsByRole(projectNum, userId, role);
            return ResponseEntity.ok(participants);
        } catch (Exception e) {
            log.error("참가자 목록 오류: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    
    
    
    
    
    
    // 클라이언트 리뷰 제출
    @PostMapping("/submitClientReview")
    public ResponseEntity<String> submitClientReview(
            @RequestBody ClientReviewDTO reviewDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String clientId = userDetails.getUsername();
            reviewDTO.setClientId(clientId); // 클라이언트 ID 설정

            if (!projectExists(reviewDTO.getProjectNum())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("존재하지 않는 프로젝트 번호입니다.");
            }

            clientReviewService.createClientReview(reviewDTO);
            return ResponseEntity.ok("클라이언트 리뷰가 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("리뷰 저장 실패");
        }
    }

    // 프리랜서 리뷰 제출
    @PostMapping("/submitFreelancerReview")
    public ResponseEntity<String> submitFreelancerReview(
            @RequestBody FreelancerReviewDTO reviewDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String freelancerId = userDetails.getUsername();
            reviewDTO.setFreelancerId(freelancerId); // 프리랜서 ID 설정

            if (!projectExists(reviewDTO.getProjectNum())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("존재하지 않는 프로젝트 번호입니다.");
            }

            freelancerReviewService.createFreelancerReview(reviewDTO);
            return ResponseEntity.ok("프리랜서 리뷰가 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("리뷰 저장 실패");
        }
    }

    // 클라이언트 또는 프리랜서가 받은 리뷰 조회
    @GetMapping("/received")
    public String getReceivedReviewsPage(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        String userId = userDetails.getUsername();
        try {
            List<ClientReviewDTO> clientReviews = clientReviewService.getReceivedReviews(userId);
            List<FreelancerReviewDTO> freelancerReviews = freelancerReviewService.getReceivedReviews(userId);

            model.addAttribute("clientReviews", clientReviews);
            model.addAttribute("freelancerReviews", freelancerReviews);

            log.info("Retrieved reviews count: 클라이언트 = {}, 프리랜서 = {}", clientReviews.size(), freelancerReviews.size());
            return "review/unifiedreview"; // 통합된 리뷰 목록 템플릿
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    // 프로젝트 존재 여부 확인
    private boolean projectExists(int projectNum) {
        return projectRepository.existsById(projectNum);
    }


}
