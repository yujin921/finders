package net.datasa.finders.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.service.MemberService;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("member/mypage")

public class MypageController {
	
	private final MemberService memberService;
	
	@GetMapping("/{memberId}")
	public String getMyPage(@PathVariable("memberId") String memberId, Model model) {

		
		return"";
	}
	

}
