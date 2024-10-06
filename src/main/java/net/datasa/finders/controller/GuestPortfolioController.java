package net.datasa.finders.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.FreelancerPortfoliosDTO;
import net.datasa.finders.security.AuthenticatedUser;
import net.datasa.finders.service.FreelancerPortfoliosService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("guestportfolio")
public class GuestPortfolioController {
	
	private final FreelancerPortfoliosService freelancerPortfoliosService;

	@PostMapping("update")
	public String updatePortfolio(@ModelAttribute FreelancerPortfoliosDTO updatedPortfolio,
	                              @AuthenticationPrincipal AuthenticatedUser user) throws Exception {
	    freelancerPortfoliosService.updatePortfolio(updatedPortfolio, user.getId());
	    return "redirect:/guestportfolio/content?portfolioId=" + updatedPortfolio.getPortfolioId();
	}

	@GetMapping("content")
    public String content(@RequestParam("portfolioId") int portfolioId, Model model) throws Exception {
		FreelancerPortfoliosDTO freelancerPortfoliosDTO = freelancerPortfoliosService.getPortfolioToGuest(portfolioId);
		model.addAttribute("freelancerPortfolios", freelancerPortfoliosDTO);

		return "guestportfolio/content"; // 모든 사용자에게 보여줄 페이지
    }
}
