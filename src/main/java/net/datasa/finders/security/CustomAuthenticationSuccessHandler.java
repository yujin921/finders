package net.datasa.finders.security;

import java.io.IOException;
import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import net.datasa.finders.domain.entity.MemberEntity;

public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
	@Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String roleName = null;

        // Determine the role based on the granted authorities
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals("ROLE_FREELANCER")) {
                roleName = "ROLE_FREELANCER";
                break;
            } else if (authority.getAuthority().equals("ROLE_CLIENT")) {
                roleName = "ROLE_CLIENT";
                break;
            } else if (authority.getAuthority().equals("ROLE_ADMIN")) {
                roleName = "ROLE_ADMIN";
                break;
            }
        }

        
        // Redirect URL based on the role
        String redirectUrl = "/"; // Default redirect URL
        String roleFromForm = request.getParameter("roleName");

        if ("ROLE_ADMIN".equals(roleName)) {
            // Admin can access any URL
        	redirectUrl = "/";
        	// redirectUrl = "/member/admin/view";
        } else if (roleFromForm != null && roleName != null && roleFromForm.equals(roleName)) {
            // User role matches the role in the form
            switch (roleName) {
                case "ROLE_FREELANCER":
                	redirectUrl = "/";
                    // redirectUrl = "/member/freelancer/view";
                    break;
                case "ROLE_CLIENT":
                	redirectUrl = "/";
                    // redirectUrl = "/member/client/view";
                    break;
            }
        } else {
            // If role does not match, redirect to error page or handle error
            redirectUrl = "/member/loginForm?error=true";
        }

        response.sendRedirect(redirectUrl);
    }
}
