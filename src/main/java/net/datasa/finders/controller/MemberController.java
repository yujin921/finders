package net.datasa.finders.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.ClientDTO;
import net.datasa.finders.domain.dto.FreelancerDTO;
import net.datasa.finders.domain.dto.MemberDTO;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.service.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("member")
public class MemberController {

    private final MemberService memberService;
    
    @GetMapping("join")
    public String join() {
        return "/member/joinForm";
    }

    @PostMapping("join")
    public String join(@RequestParam("roleName") String roleName,
    		@ModelAttribute MemberDTO member,
    		@ModelAttribute FreelancerDTO freelancer,
    		@ModelAttribute ClientDTO client) {
        	
    	  MemberEntity memberEntity = memberService.join(member);
    	  
    	  log.debug("회원가입 내용 체크용: {}", memberEntity);
    	  
    	  if ("ROLE_FREELANCER".equals(roleName)) {
              memberService.joinFreelancer(freelancer, memberEntity);
          } else if ("ROLE_CLIENT".equals(roleName)) {
              memberService.joinClient(client, memberEntity);
          }
        
        return "redirect:/";
    }
    
    @GetMapping("loginForm")
    public String loginForm() {
        return "/member/loginForm";
    }
    
    // 프리랜서 회원 페이지
    @GetMapping("freelancer/view")
    public String view1() {
        return "/member/freelancerView";
    }

    // 고객(기업) 회원 페이지
    @GetMapping("client/view")
    public String view2() {
        return "/member/clientView";
    }
    
    // 관리자 페이지
    @GetMapping("admin/view")
    public String view3() {
        return "/member/adminView";
    }
    
    // 프리랜서 마이페이지
    @GetMapping("mypageFree")
    public String mypageFree() {
    	return "/member/mypageFree";
    }
    
    // 클라이언트 마이페이지
    @GetMapping("mypageClient")
    public String mypageClient() {
    	return "/member/mypageClient";
    }
    
}
