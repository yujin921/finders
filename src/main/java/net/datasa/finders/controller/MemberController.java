package net.datasa.finders.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.ClientDTO;
import net.datasa.finders.domain.dto.FreelancerDTO;
import net.datasa.finders.domain.dto.MemberDTO;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.security.AuthenticatedUser;
import net.datasa.finders.service.MemberService;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("member")
public class MemberController {

    private final MemberService memberService;

    //application.properties 파일 관련 설정값
  	@Value("${member.uploadPath}")
  	String uploadPath;

  	
    @GetMapping("join")
    public String join() {
        return "/member/joinForm";
    }
    
    @GetMapping("idCheck")
    public String idCheck() {
    	return "/member/idCheck";
    }
    
    @PostMapping("idCheck")
    public String idCheck(
    		@RequestParam("searchId") String searchId,
    		Model model) {
    	log.debug("전달된 검색 ID : {}", searchId );
    	boolean result = memberService.idCheck(searchId);
    	
    	log.debug("전달된 ID 검색 결과 : {}", result);
    	
    	model.addAttribute("searchId", searchId);
		model.addAttribute("result", result);
		
		return "member/idCheck";
    }

    @PostMapping("join")
    public String join(@RequestParam("roleName") String roleName,
                       @ModelAttribute MemberDTO member,
                       @RequestParam("profileImg") MultipartFile profileImg,
                       @ModelAttribute FreelancerDTO freelancer,
                       @ModelAttribute ClientDTO client,
                       Model model) {
    	
    	if (profileImg != null) {
			log.debug("파일 존재 여부 : {}", profileImg.isEmpty());
			log.debug("파라미터 이름 : {}", profileImg.getName());
			log.debug("파일의 이름 : {}", profileImg.getOriginalFilename());
			log.debug("크기 : {}", profileImg.getSize());
			log.debug("파일 종류 : {}", profileImg.getContentType());
		}

        try {
            // 회원 가입 처리
            MemberEntity memberEntity = memberService.join(member, uploadPath, profileImg);

            log.debug("회원가입 내용 체크용: {}", memberEntity);

            // 역할에 따라 Freelancer 또는 Client 추가
            if ("ROLE_FREELANCER".equals(roleName)) {
                memberService.joinFreelancer(freelancer, memberEntity);
            } else if ("ROLE_CLIENT".equals(roleName)) {
                memberService.joinClient(client, memberEntity);
            }

        } catch (IOException e) {
        	log.error("파일 처리 중 오류 발생", e);

            // 오류 메시지를 모델에 추가
            model.addAttribute("errorMessage", "파일 업로드 중 오류가 발생했습니다. 다시 시도해 주세요.");

            // 회원가입 페이지로 포워딩
            return "/member/joinForm";
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
    
    // 클라이언트 마이페이지
    @GetMapping("myPage")
    public String myPage(Model model, @AuthenticationPrincipal AuthenticatedUser user) {
    	String memberId = user.getUsername();
    	MemberDTO memberDTO = memberService.getMemberInfo(memberId);
    	
    	model.addAttribute("member", memberDTO);
    return "/member/myPage";
    }
    
    @PostMapping("/update/freelancer")
    public String updateFreelancer(@ModelAttribute FreelancerDTO freelancerDTO,
    							
                               @RequestParam("profileImg") MultipartFile profileImg,
                               Model model) {
        try {
        	log.debug("{}",freelancerDTO);
            memberService.updateFreelancer(freelancerDTO, profileImg, uploadPath);
            
            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("error", "정보 수정 중 오류가 발생했습니다.");
            return "redirect:/member/myPage";
        }
    }
    
    @PostMapping("/update/client")
    public String updateClient(@ModelAttribute ClientDTO clientDTO,
                               @RequestParam("profileImg") MultipartFile profileImg,
                               Model model) {
        try {
        	log.debug("{}",clientDTO);
            memberService.updateClient(clientDTO, profileImg, uploadPath);
            
            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("error", "정보 수정 중 오류가 발생했습니다.");
            return "redirect:/member/myPage";
        }
    }
    
    
    
 // 현재 로그인한 사용자의 memberId를 반환하는 엔드포인트 추가
    @GetMapping("/getMemberId")
    public ResponseEntity<Map<String, String>> getMemberId(Principal principal) {
        // Principal 객체를 통해 현재 로그인한 사용자의 이름을 가져옵니다. 
        // 이는 일반적으로 memberId로 사용될 수 있습니다.
        String memberId = principal.getName(); // 현재 로그인한 사용자의 ID를 가져옴
        Map<String, String> response = new HashMap<>();
        response.put("memberId", memberId);

        return ResponseEntity.ok(response); // memberId를 JSON 형태로 반환
    }
}
