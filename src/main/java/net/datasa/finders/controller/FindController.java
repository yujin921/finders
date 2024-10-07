package net.datasa.finders.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.FindFreelancerDTO;
import net.datasa.finders.domain.dto.FreelancerPortfoliosDTO;
import net.datasa.finders.domain.dto.MemberDTO;
import net.datasa.finders.domain.entity.FreelancerReviewsEntity;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.domain.entity.RoleName;
import net.datasa.finders.security.AuthenticatedUser;
import net.datasa.finders.service.FindService;
import net.datasa.finders.service.FreelancerPortfoliosService;
import net.datasa.finders.service.FreelancerReviewService;
import net.datasa.finders.service.MemberService;
import net.datasa.finders.service.RecommendationService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("find")
public class FindController {

    //application.properties 파일 관련 설정값
  	@Value("${member.uploadPath}")
  	String uploadPath;
	
	private final MemberService memberService;
	private final FindService findService;
	private final FreelancerPortfoliosService freelancerPortfoliosService;
	private final FreelancerReviewService freelancerReviewService;
	private final RecommendationService recommendationService;

  	@GetMapping("view")
  	public String view(Model model, Principal principal) {
  	    if (principal != null) {
  	        // principal.getName()은 현재 로그인한 사용자의 username을 가져옴
  	        String userId = principal.getName();
  	        
  	        // MemberService를 통해 사용자의 정보 가져오기
  	        MemberEntity member = memberService.findByMemberId(userId);
  	        log.debug(member.getProfileImg());
  	        model.addAttribute("profileImgUrl", member.getProfileImg());
  	    }
  	    int countFreelancer = memberService.countFreelancer();
  	    model.addAttribute("countFreelancer", countFreelancer);
  	    
  	    return "/find/view"; // home.html 템플릿으로 이동
  	}
  	
  	@GetMapping("freelancerDetail")
  	public String freelancerDetail(@RequestParam("memberId") String memberId, Model model, @AuthenticationPrincipal AuthenticatedUser user) {
  	    if (user != null) {
  	        // 현재 로그인한 사용자의 정보 가져오기
  	        String userId = user.getId();
  	        
  	        // MemberService를 통해 사용자의 정보 가져오기
  	        MemberEntity member = memberService.findByMemberId(userId);
  	        log.debug(member.getProfileImg());
  	        model.addAttribute("profileImgUrl", member.getProfileImg());
  	    }

  	    // 프리랜서 세부 정보 및 포트폴리오, 리뷰 가져오기
  	    FindFreelancerDTO findFreelancerDTO = findService.findFreelancerDetail(memberId);
  	    List<FreelancerPortfoliosDTO> freelancerPortfoliosDTOList = freelancerPortfoliosService.findPortfolioList(memberId);
  	    List<FreelancerReviewsEntity> freelancerReviews = freelancerReviewService.getFreelancerReviewsByFreelancerId(memberId);



  	    // 모델에 데이터 추가
  	    model.addAttribute("findFreelancer", findFreelancerDTO);
  	    model.addAttribute("freelancerPortfoliosList", freelancerPortfoliosDTOList);
  	    model.addAttribute("freelancerReviews", freelancerReviews);
  	    
  	    return "find/freelancerDetail"; // 프리랜서 상세 페이지 템플릿으로 이동
  	}
  	
  	@GetMapping("applyFreelancerDetail")
  	public String applyFreelancerDetail(@RequestParam("memberId") String memberId, Model model, @AuthenticationPrincipal AuthenticatedUser user) {
  		if (user != null) {
  			// 현재 로그인한 사용자의 정보 가져오기
  			String userId = user.getId();
  			
  			// MemberService를 통해 사용자의 정보 가져오기
  			MemberEntity member = memberService.findByMemberId(userId);
  			log.debug(member.getProfileImg());
  			model.addAttribute("profileImgUrl", member.getProfileImg());
  		}
  		
  		// 프리랜서 세부 정보 및 포트폴리오, 리뷰 가져오기
  		FindFreelancerDTO findFreelancerDTO = findService.findFreelancerDetail(memberId);
  		List<FreelancerPortfoliosDTO> freelancerPortfoliosDTOList = freelancerPortfoliosService.findPortfolioList(memberId);
  		List<FreelancerReviewsEntity> freelancerReviews = freelancerReviewService.getFreelancerReviewsByFreelancerId(memberId);
  		
  		
  		
  		// 모델에 데이터 추가
  		model.addAttribute("findFreelancer", findFreelancerDTO);
  		model.addAttribute("freelancerPortfoliosList", freelancerPortfoliosDTOList);
  		model.addAttribute("freelancerReviews", freelancerReviews);
  		
  		return "find/applyFreelancerDetail"; // 프리랜서 상세 페이지 템플릿으로 이동
  	}
  	
  	@ResponseBody
    @GetMapping("currentUser")
	public Map<String, Object> getCurrentUser(@AuthenticationPrincipal AuthenticatedUser user) {
	    MemberDTO memberDTO = recommendationService.getCurrentUser(user.getUsername());

	    Map<String, Object> response = new HashMap<>();
	    response.put("roleName", memberDTO.getRoleName());
	    
	    // 클라이언트와 프리랜서 ID를 추가
	    if (memberDTO.getRoleName() == RoleName.ROLE_CLIENT) {
	        response.put("clientId", memberDTO.getMemberId()); // 클라이언트 ID
	    } else if (memberDTO.getRoleName() == RoleName.ROLE_FREELANCER) {
	        response.put("freelancerId", memberDTO.getMemberId()); // 프리랜서 ID
	    }
	    
	    return response; // JSON 형식으로 응답
	}
  	
  	// 클라이언트가 프리랜서를 추천받기 위한 API 엔드포인트
  	@ResponseBody
    @GetMapping("recommendations/freelancers")
    public List<FindFreelancerDTO> recommendFreelancers(@RequestParam("clientId") String clientId) {
        // 클라이언트 ID를 이용해 추천 프리랜서를 가져옴
        return recommendationService.recommendFreelancers(clientId);
    }
}
