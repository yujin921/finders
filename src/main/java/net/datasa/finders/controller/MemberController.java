package net.datasa.finders.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.MemberDTO;
import net.datasa.finders.service.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("member")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("loginForm")
    public String loginForm() {
        return "/member/loginForm";
    }

    @GetMapping("join")
    public String join() {
        return "/member/joinForm";
    }

    @PostMapping("join")
    public String join(@ModelAttribute MemberDTO member) {
        memberService.join(member);
        return "redirect:/";
    }
}
