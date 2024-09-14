package net.datasa.finders.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.ProjectPublishingDTO;
import net.datasa.finders.domain.entity.ApplicationResult;
import net.datasa.finders.domain.entity.RoleName;
import net.datasa.finders.security.AuthenticatedUser;
import net.datasa.finders.service.BoardService;
import net.datasa.finders.service.ProjectApplicationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("board")
public class BoardController {
	
	private final BoardService boardService;
    private final ProjectApplicationService projectApplicationService;
	
	@GetMapping("view")
	public String view(@AuthenticationPrincipal AuthenticatedUser user
			,Model model) {
		model.addAttribute("profileImgUrl", user.getProfileImg());
		return "board/list";
	}
	
	@GetMapping("write")
    public String write() {
        return "board/writeForm";
    }

    @ResponseBody
    @GetMapping("list")
    public List<ProjectPublishingDTO> list(@AuthenticationPrincipal AuthenticatedUser user) {
        //서비스로 사용자 아이디를 전달하여 해당 아이디의 수입,지출 내역을 목록으로 리턴한다.
        List<ProjectPublishingDTO> list = boardService.getList(user.getUsername());
        return list;
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

        return "redirect:view";
    }

    @GetMapping("read")
    public String read(@RequestParam("projectNum") int projectNum, Model model, @AuthenticationPrincipal AuthenticatedUser user, Principal principal) {
        try {
            // 사용자 역할 확인
            RoleName roleName = RoleName.valueOf(user.getRoleName());
            log.debug("현재 사용자의 역할: {}", roleName);

            // 게시물 정보 불러오기
            ProjectPublishingDTO projectPublishingDTO = boardService.getBoard(projectNum, user.getUsername(), roleName);
            model.addAttribute("board", projectPublishingDTO);
            model.addAttribute("user", user);
            model.addAttribute("roleName", projectPublishingDTO.getRoleName());

            // 프리랜서의 신청 상태 조회
            String freelancerUsername = principal.getName();  // 현재 로그인한 사용자 (프리랜서)
            boolean applied = projectApplicationService.hasApplied(projectNum, freelancerUsername);
            model.addAttribute("applied", applied);

            // 지원 상태가 있으면 설정
            if (applied) {
                String applicationStatus = projectApplicationService.getApplicationStatus(projectNum, freelancerUsername);
                model.addAttribute("applicationStatus", applicationStatus);
            }

            return "board/read";  // 'read.html'로 반환
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/board/view";  // 오류 발생 시 목록으로 리다이렉트
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
                return "redirect:/board/view"; // 게시글 목록 페이지로 리다이렉트
            } else {
                return "redirect:/board/view?error=deletePermission"; // 권한 오류 페이지로 리다이렉트
            }
        } catch (Exception e) {
            return "redirect:/board/view?error=deleteFailed"; // 삭제 실패 페이지로 리다이렉트
        }
    }

    @PostMapping("apply")
    public String applyToProject(@RequestParam("projectNum") int projectNum, Principal principal) {
        String freelancerUsername = principal.getName();  // 로그인한 프리랜서의 사용자 이름 (ID)
        log.debug("{}", freelancerUsername);
        projectApplicationService.applyToProject(projectNum, freelancerUsername);  // 프리랜서와 프로젝트 번호를 사용하여 신청 저장
        log.debug("{}", projectApplicationService);
        return "redirect:/board/read?projectNum=" + projectNum;  // 신청 후 페이지 리디렉션
    }

    @PostMapping("update-status")
    public String updateApplicationStatus(@RequestParam("projectNum") int projectNum
            , @RequestParam("freelancerUsername") String freelancerUsername
            , @RequestParam("status") String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status is missing or empty");
        }

        log.debug("Received status value: {}", status);
        ApplicationResult result = ApplicationResult.valueOf(status.toUpperCase());  // 'accepted', 'rejected' 등 입력값을 ENUM으로 변환

        projectApplicationService.updateApplicationStatus(projectNum, freelancerUsername, result);  // 상태 업데이트

        return "redirect:/board/read?projectNum=" + projectNum;  // 업데이트 후 페이지 리디렉션
    }
}