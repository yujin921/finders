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
@RequestMapping("portfolio")
public class PortfolioController {

	@GetMapping("edit")
    public String protfolio() {
		
        return "/portfolio/portfolio";
    }
}
