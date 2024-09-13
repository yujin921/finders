package net.datasa.finders.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Slf4j
@RequiredArgsConstructor
@Controller
public class HomeController {
	
    //application.properties 파일 관련 설정값
  	@Value("${member.uploadPath}")
  	String uploadPath;
	
	@Autowired
	private MemberService memberService;

  	@GetMapping({"","/"})
  	public String homePage(Model model, Principal principal) {
  	    if (principal != null) {
  	        // principal.getName()은 현재 로그인한 사용자의 username을 가져옴
  	        String userId = principal.getName();
  	        
  	        // MemberService를 통해 사용자의 정보 가져오기
  	        MemberEntity member = memberService.findByMemberId(userId);
  	        log.debug(member.getProfileImg());
  	        model.addAttribute("profileImgUrl", member.getProfileImg());
  	    }
  	    return "home"; // home.html 템플릿으로 이동
  	}
}