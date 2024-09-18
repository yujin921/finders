package net.datasa.finders.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.ClientDataDTO;
import net.datasa.finders.domain.dto.ClientReviewDTO;
import net.datasa.finders.domain.dto.FreelancerDataDTO;
import net.datasa.finders.domain.dto.FreelancerReviewDTO;
import net.datasa.finders.repository.ProjectPublishingRepository;
import net.datasa.finders.service.ClientReviewService;
import net.datasa.finders.service.FreelancerReviewService;
import net.datasa.finders.service.ReviewService;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/review")
public class ReviewController {

    private final ReviewService reviewService;
    private final FreelancerReviewService freelancerReviewService;
    private final ClientReviewService clientReviewService;
    private final ProjectPublishingRepository projectRepository;
    
    @PostMapping("/submitReview")
    public ResponseEntity<String> submitReview(
            @RequestBody FreelancerReviewDTO reviewDTO, 
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // 클라이언트 ID를 UserDetails에서 가져와 설정
            String clientId = userDetails.getUsername();
            reviewDTO.setClientId(clientId); // DTO에 클라이언트 ID 설정

            // createFreelancerReview 호출
            reviewService.createFreelancerReview(reviewDTO);
            return ResponseEntity.ok("리뷰가 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("리뷰 저장 실패");
        }
    }


    @GetMapping("/getReviewData")
    public ResponseEntity<FreelancerReviewDTO> getReviewData(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("projectNum") int projectNum) {
        String clientId = userDetails.getUsername();

        try {
            FreelancerReviewDTO reviewData = reviewService.getReviewData(projectNum, clientId);
            return ResponseEntity.ok(reviewData);
        } catch (Exception e) {
            log.error("리뷰 데이터를 가져오는 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/getFreelancers")
    public ResponseEntity<?> getFreelancers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("projectNum") int projectNum) {
        String clientId = userDetails.getUsername();
        log.info("getFreelancers 메서드 호출됨: projectNum = {}, clientId = {}", projectNum, clientId);

        try {
            // FreelancerReviewService의 getFreelancersByProject 호출
            List<FreelancerDataDTO> freelancers = freelancerReviewService.getFreelancersByProject(projectNum, clientId);
            log.info("프리랜서 목록: {}", freelancers);
            return ResponseEntity.ok(freelancers);
        } catch (Exception e) {
            log.error("프리랜서 목록을 가져오는 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("프리랜서 목록을 가져오는 중 오류가 발생했습니다.");
        }
    }
    
    @GetMapping("/getParticipants")
    public ResponseEntity<List<ClientDataDTO>> getParticipants(
            @RequestParam("projectNum") int projectNum,
            @AuthenticationPrincipal UserDetails userDetails) {
        String clientId = userDetails.getUsername();
        log.info("getParticipants 메서드 호출됨: projectNum = {}, clientId = {}", projectNum, clientId);

        try {
            List<ClientDataDTO> participants = clientReviewService.getParticipantsByProject(projectNum, clientId);

            // 각 참가자의 리뷰 작성 완료 여부를 확인하여 설정
            for (ClientDataDTO participant : participants) {
                // participant.getId() 대신 freelancerId를 사용해야 할 수 있음
                boolean isReviewCompleted = clientReviewService.isReviewCompleted(participant.getId(), projectNum, clientId);
                participant.setReviewCompleted(isReviewCompleted); // 작성 완료 상태 설정
                log.info("참가자 ID: {}, 리뷰 작성 완료 여부: {}", participant.getId(), isReviewCompleted); // 로그 추가
            }

            return ResponseEntity.ok(participants);
        } catch (Exception e) {
            log.error("참가자 목록을 가져오는 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @PostMapping("/submitClientReview")
    public ResponseEntity<String> submitClientReview(
            @RequestBody ClientReviewDTO reviewDTO, 
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String clientId = userDetails.getUsername();
            reviewDTO.setClientId(clientId); // DTO에 클라이언트 ID 설정

            // 클라이언트 리뷰가 이미 작성되었는지 확인
            if (clientReviewService.isReviewCompleted(clientId, reviewDTO.getProjectNum(), reviewDTO.getFreelancerId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이미 리뷰를 작성하였습니다.");
            }

            // 필요한 검증: projectNum이 존재하는지 확인
            if (!projectExists(reviewDTO.getProjectNum())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("존재하지 않는 프로젝트 번호입니다.");
            }

            // 클라이언트 리뷰 저장
            clientReviewService.createClientReview(reviewDTO);
            return ResponseEntity.ok("리뷰가 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("리뷰 저장 실패");
        }
    }


    // 프로젝트가 존재하는지 확인하는 메서드
    private boolean projectExists(int projectNum) {
        return projectRepository.existsById(projectNum);
    }
}
