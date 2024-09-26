package net.datasa.finders.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.UnifiedReviewDTO;
import net.datasa.finders.service.FreelancerReviewService;

//채팅전용
//테스트를 위한 project 관리
@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("freelancerreview")
public class FreelancerReviewController {

	
    private final FreelancerReviewService freelancerReviewService;

    // 프리랜서가 받은 모든 리뷰를 조회하는 메서드 (클라이언트와 프리랜서 모두 포함)
    @GetMapping("/received")
    public String getFreelancerReviewsPage(
            @AuthenticationPrincipal UserDetails userDetails, 
            Model model) {
        String freelancerId = userDetails.getUsername(); // 프리랜서 ID 가져오기
        try {
            // 통합된 리뷰 DTO 리스트 조회 (클라이언트와 프리랜서가 남긴 리뷰 모두 포함)
            
            return "review/freelancerreview"; // Thymeleaf 템플릿 이름
        } catch (Exception e) {
            e.printStackTrace();
            return "error"; // 에러 페이지로 리다이렉트
        }
    }

    
    @GetMapping("/writefreelancerreview")
    public String writefreelancerReview(@RequestParam("projectNum") int projectNum, Model model) {
        // 프로젝트 번호를 모델에 추가
        model.addAttribute("projectNum", projectNum);
        // 다른 필요한 데이터도 추가 가능

        // freelancerreview 페이지로 이동
        return "review/writefreelancerreview"; // Thymeleaf 템플릿 이름
    }
	

}
