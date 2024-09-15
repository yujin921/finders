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
import net.datasa.finders.domain.dto.FreelancerReviewDTO;
import net.datasa.finders.domain.entity.ProjectEntity;
import net.datasa.finders.service.FreelancerReviewService;
import net.datasa.finders.service.ProjectService;

//채팅전용
//테스트를 위한 project 관리
@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("review")
public class FreelancerReviewController {

	
    private final FreelancerReviewService freelancerReviewService;

    @GetMapping("/received")
    public String getReceivedReviewsPage(
            @AuthenticationPrincipal UserDetails userDetails, 
            Model model) {
        String freelancerId = userDetails.getUsername();
        try {
            List<FreelancerReviewDTO> reviews = freelancerReviewService.getReceivedReviews(freelancerId);
            model.addAttribute("reviews", reviews);
            return "/review/freelancerreview"; // Thymeleaf 템플릿 이름
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
