package net.datasa.finders.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_FREELANCER"))) {
            if (request.getRequestURI().equals("/board/write")) {
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().println("<script>alert('프로젝트 등록은 클라이언트 계정만 가능합니다'); history.back();</script>");
                return;
            }
        }
        response.sendRedirect("/access-denied");
    }
}
