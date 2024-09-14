package net.datasa.finders.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.ProjectPublishingDTO;
import net.datasa.finders.domain.entity.RoleName;
import net.datasa.finders.security.AuthenticatedUser;
import net.datasa.finders.service.ProjectManagementService;

/**
 * 거래 게시판 관련 콘트롤러
 */

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("myProject")
public class ProjectManagementController {
	
	private final ProjectManagementService projectManagementService;
	
	@GetMapping("view")
	public String view() {
		return "project/listView";
	}
	
	@ResponseBody
    @GetMapping("projectList")
    public List<ProjectPublishingDTO> projectList(@AuthenticationPrincipal AuthenticatedUser user) {
        //서비스로 사용자 아이디를 전달하여 해당 아이디의 수입,지출 내역을 목록으로 리턴한다.
        List<ProjectPublishingDTO> projectList = projectManagementService.getMyList(user.getUsername());
        
        return projectList;
    }
    
    @GetMapping("management")
	public String read(@RequestParam("projectNum") int pNum
			, Model model
			, @AuthenticationPrincipal AuthenticatedUser user) {
	    try {
            RoleName roleName = RoleName.valueOf(user.getRoleName());

            log.debug("현재 사용자의 역할: {}", roleName);
	        ProjectPublishingDTO projectPublishingDTO = projectManagementService.getBoard(pNum, user.getUsername(), roleName);
	        model.addAttribute("board", projectPublishingDTO);
            model.addAttribute("user", user);
            model.addAttribute("roleName", projectPublishingDTO.getRoleName());
	        return "project/management";
	    } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/myProject/view";
	    }
	}

    // 임시 리스트 화면 구현 시 기존 프로젝트 생성 페이지 Controller 코드
    /*
    @GetMapping("write")
    public String write() {
        return "project/writeForm";
    }
     
    @PostMapping("write")
    public String write(
            @ModelAttribute ProjectPublishingDTO projectPublishingDTO,
            @RequestParam("projectImageFile") MultipartFile projectImageFile, // 이미지 파일
            @RequestParam("selectedSkills") String selectedSkills,  // 관련 기술
            @RequestParam("projectDescription") String projectDescription,  // 상세 업무 내용
            @RequestParam("projectBudget") BigDecimal projectBudget,  // 지출 예산
            @RequestParam("projectStartDate") LocalDate projectStartDate,  // 프로젝트 시작일
            @RequestParam("projectEndDate") LocalDate projectEndDate,  // 프로젝트 종료일
            @RequestParam("recruitDeadline") LocalDateTime recruitDeadline,
            @RequestParam("role") List<String> roles, // 모집 인원 역할
            @RequestParam("category") List<String> categories, // 모집 인원 카테고리
            @RequestParam("teamSize[]") List<Integer> teamSizes, // 모집 인원
            @RequestParam("question[]") List<String> questions, // 사전 질문
            @AuthenticationPrincipal AuthenticatedUser user) {

        // 작성한 글에 사용자 아이디 추가
        projectPublishingDTO.setClientId(user.getUsername());

        // BoardService 호출해서 프로젝트 및 관련 데이터 저장
        boardService.write(projectPublishingDTO, projectImageFile, selectedSkills
                , projectDescription, projectBudget, projectStartDate, projectEndDate, recruitDeadline, roles, categories, teamSizes, questions);

        return "redirect:/myProject/view";
    }

	@GetMapping("read")
	public String read(@RequestParam("projectNum") int pNum, Model model, @AuthenticationPrincipal AuthenticatedUser user) {
	    try {
            RoleName roleName = RoleName.valueOf(user.getRoleName());

            log.debug("현재 사용자의 역할: {}", roleName);
	        ProjectPublishingDTO projectPublishingDTO = boardService.getBoard(pNum, user.getUsername(), roleName);
	        model.addAttribute("board", projectPublishingDTO);
            model.addAttribute("user", user);
            model.addAttribute("roleName", projectPublishingDTO.getRoleName());
	        return "project/read";
	    } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/myProject/view";
	    }
	}

	@PostMapping("delete")
    public String deletePost(@RequestParam("projectNum") int pNum, @AuthenticationPrincipal AuthenticatedUser user) {
        try {
            RoleName roleName = RoleName.valueOf(user.getRoleName());
            ProjectPublishingDTO projectPublishingDTO = boardService.getBoard(pNum, user.getUsername(), roleName);

            // 로그인된 사용자와 게시글 작성자가 같은지 확인
            if (projectPublishingDTO.getClientId().equals(user.getUsername())) {
                boardService.deleteBoard(pNum);
                return "redirect:/myProject/view"; // 게시글 목록 페이지로 리다이렉트
            } else {
                return "redirect:/myProject/view?error=deletePermission"; // 권한 오류 페이지로 리다이렉트
            }
        } catch (Exception e) {
            return "redirect:/myProject/view?error=deleteFailed"; // 삭제 실패 페이지로 리다이렉트
        }
    }
    */
}
