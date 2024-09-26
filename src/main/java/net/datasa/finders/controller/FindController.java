package net.datasa.finders.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.FindFreelancerDTO;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.security.AuthenticatedUser;
import net.datasa.finders.service.FindService;
import net.datasa.finders.service.MemberService;

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
  	    return "/find/view"; // home.html 템플릿으로 이동
  	}
  	
  	@GetMapping("freelancerDetail")
  	public String freelancerDetail(@RequestParam("memberId") String memberId
  			, Model model
  			, @AuthenticationPrincipal AuthenticatedUser user) {
  	    if(user == null) {
  	    	return "/member/loginForm";
  	    }
  	    FindFreelancerDTO findFreelancerDTO = findService.findFreelancerDetail(memberId);
  	    
  	    
  	    
  	    return "/find/freelancerDetail"; // home.html 템플릿으로 이동
  	}
}
