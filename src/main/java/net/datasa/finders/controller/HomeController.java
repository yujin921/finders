package net.datasa.finders.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.repository.MemberRepository;

@Slf4j
@RequiredArgsConstructor
@Controller
public class HomeController {
	
	@Autowired
	private MemberRepository memberRepository;
	
    @GetMapping({"","/"})
    public String home(Model model, Principal principal) {
    	
    	String userId = principal.getName();
    	
    	MemberEntity member = memberRepository.findByMemberId(userId);
    	
    	String profileImgUrl = member != null ? member.getProfileImg() : "https://i.namu.wiki/i/Bge3xnYd4kRe_IKbm2uqxlhQJij2SngwNssjpjaOyOqoRhQlNwLrR2ZiK-JWJ2b99RGcSxDaZ2UCI7fiv4IDDQ.webp";
    	
    	model.addAttribute("profileImgUrl", profileImgUrl);
    	
        return "home";
    }
}