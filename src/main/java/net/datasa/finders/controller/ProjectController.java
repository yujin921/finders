package net.datasa.finders.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.service.MemberService;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("project")
public class ProjectController {

	@GetMapping("view")
    public String protfolio() {
		
        return "/project/view";
    }
	
	@GetMapping("list")
    public String list() {
		
        return "/project/list";
    }
	
	@GetMapping("management")
    public String management() {
		
        return "/project/management";
    }
}
