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
import net.datasa.finders.domain.dto.ClientReviewDTO;
import net.datasa.finders.service.ClientReviewService; // 클라이언트 리뷰 서비스

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("clientreview")
public class ClientReviewController {

    private final ClientReviewService clientReviewService; // 클라이언트 리뷰 서비스

    @GetMapping("/received")
    public String getReceivedReviewsPage(
            @AuthenticationPrincipal UserDetails userDetails, 
            Model model) {
        String clientId = userDetails.getUsername(); // 클라이언트 ID
        try {
            List<ClientReviewDTO> reviews = clientReviewService.getReceivedReviews(clientId); // 클라이언트 리뷰 가져오기
            model.addAttribute("reviews", reviews);
            log.info("Retrieved reviews count: {}", reviews.size());
            return "review/clientreview"; // 클라이언트 리뷰 템플릿 이름
        } catch (Exception e) {
            e.printStackTrace();
            return "error"; // 에러 페이지로 리다이렉트
        }
    }
    
    @GetMapping("/writeclientreview")
    public String writeClientReview(@RequestParam("projectNum") int projectNum, Model model) {
        model.addAttribute("projectNum", projectNum);
        return "review/writeclientreview"; // 클라이언트 리뷰 작성 페이지 템플릿 이름
    }
}
