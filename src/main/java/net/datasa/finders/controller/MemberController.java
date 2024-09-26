package net.datasa.finders.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.ClientDTO;
import net.datasa.finders.domain.dto.FreelancerDTO;
import net.datasa.finders.domain.dto.FreelancerPortfoliosDTO;
import net.datasa.finders.domain.dto.FreelancerSkillDTO;
import net.datasa.finders.domain.dto.MemberDTO;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.security.AuthenticatedUser;
import net.datasa.finders.service.FreelancerPortfoliosService;
import net.datasa.finders.service.MemberService;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("member")
public class MemberController {

    private final MemberService memberService;
	private final FreelancerPortfoliosService freelancerPortfoliosService;

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
    public String join(@ModelAttribute MemberDTO member,
                       @RequestParam("profileImg") MultipartFile profileImg,
                       @ModelAttribute FreelancerDTO freelancer,
                       @ModelAttribute ClientDTO client,
                       @RequestParam(value = "selectedSkills", required = false) List<String> selectedSkills,
                       @RequestParam(value = "selectedField", required = false) List<String> selectedField,
                       @RequestParam(value = "selectedCategory", required = false) List<String> selectedCategory,
                       Model model) {
        try {
            memberService.join(member, uploadPath, profileImg);
            
            switch (member.getRoleName()) {
                case ROLE_FREELANCER:
                    memberService.joinFreelancer(freelancer, member);
                    // 프리랜서 스킬 저장
                    if (selectedSkills != null && !selectedSkills.isEmpty()) {
                        memberService.updateFreelancerSkills(member.getMemberId(), selectedSkills);
                    }
                    if (selectedField != null && !selectedField.isEmpty()) {
                    	memberService.updateClientField(member.getMemberId(), selectedField);
                    }
                    if (selectedCategory != null && !selectedCategory.isEmpty()) {
                    	memberService.updateClientCategory(member.getMemberId(), selectedCategory);
                    }
                    break;
                case ROLE_CLIENT:
                    memberService.joinClient(client, member);
                    // 클라이언트 관심 분야 저장
                    if (selectedField != null && !selectedField.isEmpty()) {
                    	memberService.updateClientField(member.getMemberId(), selectedField);
                    }
                    if (selectedCategory != null && !selectedCategory.isEmpty()) {
                    	memberService.updateClientCategory(member.getMemberId(), selectedCategory);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Invalid role: " + member.getRoleName());
            }
        } catch (IOException e) {
            log.error("파일 처리 중 오류 발생", e);
            model.addAttribute("errorMessage", "파일 업로드 중 오류가 발생했습니다. 다시 시도해 주세요.");
            return "/member/joinForm";
        }

        return "redirect:/";
    }
    
    @GetMapping("loginForm")
    public String loginForm() {
        return "/member/loginForm";
    }
    
    
    // 관리자 페이지
    @GetMapping("admin/view")
    public String view3() {
        return "/member/adminView";
    }
    
    // 클라이언트 마이페이지
    @GetMapping("myPage")
    public String myPage(Model model, @AuthenticationPrincipal AuthenticatedUser user) {
    	String memberId = user.getUsername();
    	MemberDTO memberDTO = memberService.getMemberInfo(memberId);

    	List<FreelancerPortfoliosDTO> freelancerPortfoliosDTOList = freelancerPortfoliosService.findPortfolioList(memberId);
		
		model.addAttribute("portfoliosList", freelancerPortfoliosDTOList);
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
                               @RequestParam(value = "selectedField", required = false) String selectedFieldString,
                               @RequestParam(value = "selectedCategory", required = false) String selectedCategoryString,
                               Model model) {
        try {
        	if (selectedFieldString != null && !selectedFieldString.isEmpty()) {
                clientDTO.setFields(Arrays.asList(selectedFieldString.split(",")));
            }
            if (selectedCategoryString != null && !selectedCategoryString.isEmpty()) {
                clientDTO.setCategorys(Arrays.asList(selectedCategoryString.split(",")));
            }
            memberService.updateClient(clientDTO, profileImg, uploadPath);
            return "redirect:/";
        } catch (Exception e) {
        	log.debug("{}", clientDTO);
            model.addAttribute("error", "정보 수정 중 오류가 발생했습니다.");
            return "redirect:/member/myPage";
        }
    }
    
    @GetMapping("findId")
    public String findId() {
    	return "/member/findId";
    }
    
    // 아이디 찾기 요청을 처리하는 POST 메서드
    @PostMapping("/findId")
    public String findId(@RequestParam("memberName") String memberName,
                         @RequestParam("email") String email,
                         Model model) {
        try {
            // 서비스에서 이름과 이메일을 기반으로 아이디를 찾음
            String memberId = memberService.findUsernameBymemberNameAndEmail(memberName, email);
            // 아이디 찾기 성공 시 메시지를 모델에 추가
            model.addAttribute("message", "회원님의 아이디는: " + memberId);
        } catch (Exception e) {
            // 아이디를 찾지 못한 경우 에러 메시지를 모델에 추가
            model.addAttribute("error", "해당 정보를 가진 사용자가 없습니다.");
        }
        return "/member/findIdResult"; // 결과를 보여줄 페이지 (Thymeleaf 템플릿 이름)
    }

    @GetMapping("findIdResult")
    public String findIdResult() {
    	return "/member/findIdResult";
    }
    
    @GetMapping("resetPw")
    public String resetPw() {
    	return "/member/verifyUser";
    }
    
    @PostMapping("verifyUser")
    public String verifyUser(@RequestParam("memberId") String memberId,
                             @RequestParam("memberName") String memberName,
                             @RequestParam("email") String email,
                             Model model) {
        log.info("Verifying user: id={}, name={}, email={}", memberId, memberName, email);
        boolean isVerified = memberService.verifyUser(memberId, memberName, email);
        log.info("User verification result: {}", isVerified);
        
        if (isVerified) {
            model.addAttribute("memberId", memberId);
            return "/member/resetPassword";
        } else {
            model.addAttribute("error", "일치하는 회원 정보를 찾을 수 없습니다.");
            return "/member/verifyUser";
        }
    }
    
    @PostMapping("resetPassword")
    public String resetPassword(@RequestParam("memberId") String memberId,
                                @RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                Model model) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            model.addAttribute("memberId", memberId);
            return "/member/resetPassword";
        }

        if (memberService.resetPassword(memberId, newPassword)) {
            return "redirect:/";
        } else {
            model.addAttribute("error", "비밀번호 재설정에 실패했습니다.");
            return "/member/resetPassword";
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
