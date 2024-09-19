package net.datasa.finders.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.datasa.finders.domain.entity.MemberEntity;
import net.datasa.finders.service.MemberService;

@Component
public class UserInfoInterceptor implements HandlerInterceptor {

    @Autowired
    private MemberService memberService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        if (modelAndView != null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
                String userId = auth.getName();
                MemberEntity member = memberService.findByMemberId(userId);
                modelAndView.addObject("profileImgUrl", member.getProfileImg());
            }
        }
    }
}