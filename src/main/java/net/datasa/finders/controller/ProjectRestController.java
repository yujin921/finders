package net.datasa.finders.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.ProjectDTO;
import net.datasa.finders.security.AuthenticatedUser;
import net.datasa.finders.service.ProjectService;

//채팅전용
//테스트를 위한 project 관리
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("project")
public class ProjectRestController {

    private final ProjectService projectService; // ProjectService 주입

	@GetMapping("projectList")
	public List<ProjectDTO> projectList(@RequestParam("status") String status
			,@AuthenticationPrincipal AuthenticatedUser user
			,Model model) {
		List<ProjectDTO> projectDTOList = projectService.findProjectsByMemberIdAndStatus(user.getId(), status);
		
		model.addAttribute("projects", projectDTOList);
		return projectDTOList;
	}

}
