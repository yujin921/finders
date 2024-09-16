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
import net.datasa.finders.domain.entity.ProjectEntity;
import net.datasa.finders.service.ProjectService;

//채팅전용
//테스트를 위한 project 관리
@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("project")
public class ProjectController {

    private final ProjectService projectService; // ProjectService 주입

	@GetMapping("view")
    public String protfolio() {
		
        return "/project/view";
    }
	
	@GetMapping("list")
	public String list(@AuthenticationPrincipal UserDetails userDetails, Model model) {
	    String memberId = userDetails.getUsername();
	    System.out.println("Authenticated Member ID: " + memberId); // 로그 추가

	    List<ProjectEntity> projects = projectService.getProjectsByMemberId(memberId);
	    System.out.println("Projects retrieved: " + projects.size()); // 조회된 프로젝트 수 확인

	    model.addAttribute("projects", projects);
	    return "/project/list";
	}	

}
