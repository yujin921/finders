package net.datasa.finders.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.ProjectPublishingDTO;
import net.datasa.finders.domain.entity.ClientReviewsEntity;
import net.datasa.finders.domain.entity.RoleName;
import net.datasa.finders.security.AuthenticatedUser;
import net.datasa.finders.service.ClientReviewService;
import net.datasa.finders.service.ProjectApplicationService;
import net.datasa.finders.service.ProjectPublishingService;
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
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("board")
public class BoardController {
	
	private final ProjectPublishingService projectPublishingService;
    private final ProjectApplicationService projectApplicationService;
    private final ClientReviewService clientReviewService;
	
	@GetMapping("view")
	public String view() {
		return "board/list";
	}
	
	@GetMapping("write")
    public String write() {
        return "board/writeForm";
    }

	
    @ResponseBody
    @GetMapping("list")
    public List<ProjectPublishingDTO> list(@RequestParam("word") String word) {
        List<ProjectPublishingDTO> list = projectPublishingService.getList(word);

        for (ProjectPublishingDTO project : list) {
            // 프로젝트를 등록한 클라이언트의 평균 평점 구하기
            Optional<Float> averageRating = clientReviewService.getAverageRatingForClient(project.getClientId());
            project.setAverageRating(averageRating.orElse(0.0f));  // 평점이 없을 경우 0점 설정
        }

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
        projectPublishingService.write(projectPublishingDTO, projectImageFile, selectedSkills
                , projectDescription, projectBudget, projectStartDate, projectEndDate, recruitDeadline, roles, categories, teamSizes, questions);

        return "redirect:view";
    }

    @GetMapping("read")
    public String read(@RequestParam("projectNum") int projectNum, Model model, @AuthenticationPrincipal AuthenticatedUser user, Principal principal) {
        try {
            // 사용자 역할 확인
            RoleName roleName = RoleName.valueOf(user.getRoleName());

            // 게시물 정보 불러오기
            ProjectPublishingDTO projectPublishingDTO = projectPublishingService.getBoard(projectNum, user.getUsername(), roleName);
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
            
            // 클라이언트 ID 기반으로 후기를 가져옴 (게시글 작성자의 ID)
            List<ClientReviewsEntity> clientReviews = clientReviewService.getClientReviewsByClientId( projectPublishingDTO.getClientId());
            model.addAttribute("clientReviews", clientReviews);

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
            ProjectPublishingDTO projectPublishingDTO = projectPublishingService.getBoard(pNum, user.getUsername(), roleName);

            // 로그인된 사용자와 게시글 작성자가 같은지 확인
            if (projectPublishingDTO.getClientId().equals(user.getUsername())) {
                projectPublishingService.deleteBoard(pNum);
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

    @GetMapping("/update")
    public String showUpdateForm(@RequestParam("projectNum") int projectNum, Model model) {
        ProjectPublishingDTO board = projectPublishingService.getBoardByProjectNum(projectNum); // 프로젝트 번호로 게시글 데이터 가져오기
        model.addAttribute("board", board); // 모델에 기존 데이터를 담아서 수정 페이지로 보냄
        return "board/updateForm"; // 수정 페이지로 이동
    }

    @PostMapping("/update")
    public String updateBoard(@ModelAttribute ProjectPublishingDTO projectPublishingDTO,
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
                              @AuthenticationPrincipal AuthenticatedUser user){
        projectPublishingDTO.setClientId(user.getUsername());

        projectPublishingService.updateBoard(projectPublishingDTO, projectImageFile, selectedSkills, projectDescription, projectBudget,
                projectStartDate, projectEndDate, recruitDeadline, roles, categories, teamSizes, questions);

        return "redirect:/board/view?projectNum=" + projectPublishingDTO.getProjectNum(); // 수정 후 해당 게시글로 리다이렉트
    }

    @ResponseBody
    @GetMapping("latestProjects")
    public List<ProjectPublishingDTO> latestProjects() {
        return projectPublishingService.getLatestProjects(4);  // 최신 4개 프로젝트 가져오기
    }
}