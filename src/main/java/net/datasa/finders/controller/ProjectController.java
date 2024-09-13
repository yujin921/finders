package net.datasa.finders.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//채팅전용
//테스트를 위한 project 관리
@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("project")
public class ProjectController {

	@GetMapping("view")
    public String protfolio() {
		
        return "/project/view";
    }
	
	@GetMapping("myProjectList")
    public String myProjectList() {
		
        return "/project/myProjectList";
	}

	@GetMapping("view2")
    public String protfolio2() {
		
        return "/project/view2";
    }
	
	@GetMapping("management")
    public String management() {
		
        return "/project/management";
    }
}
