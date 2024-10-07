package net.datasa.finders.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.FindFreelancerDTO;
import net.datasa.finders.domain.dto.FreelancerPortfoliosDTO;
import net.datasa.finders.domain.dto.ProjectPublishingDTO;
import net.datasa.finders.security.AuthenticatedUser;
import net.datasa.finders.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Controller
public class HomeController {
	
	private final FreelancerPortfoliosService freelancerPortfoliosService;
	private final ProjectPublishingService projectPublshingService;
	private final FindService findService;
	private final ClientReviewService clientReviewService;
	
    //application.properties 파일 관련 설정값
  	@Value("${member.uploadPath}")
  	String uploadPath;
	

  	@GetMapping({"","/"})
  	public String homePage() {
  	    return "home"; // home.html 템플릿으로 이동
  	}
  	
  	@GetMapping("header")
  	public String header() {
  	    return "header";
  	}
  	
  	@GetMapping("nav")
  	public String nav() {
  		return "nav";
  	}
  	
  	@GetMapping("footer")
  	public String footer() {
  		return "footer";
  	}
  	
  	@GetMapping("aside")
  	public String aside(@AuthenticationPrincipal AuthenticatedUser user
    		,Model model) {
		List<FreelancerPortfoliosDTO> freelancerPortfoliosDTOList = freelancerPortfoliosService.findPortfolioList(user.getId());
		
		model.addAttribute("portfoliosList", freelancerPortfoliosDTOList);
  		return "mypageSidebar";
  	}
  	
  	@GetMapping("base")
  	public String base() {
  		return "baseHTML";
  	}
  	
  	@GetMapping("totalSearch")
  	public String totalSearch(@RequestParam(value = "keyword", defaultValue = "") String totalSearch, Model model) {
  		
  		log.debug("지나감");
  		List<ProjectPublishingDTO> projectPubloshingDTOList = projectPublshingService.getList(totalSearch);
  		
  		 for (ProjectPublishingDTO project : projectPubloshingDTOList) {
             // 프로젝트를 등록한 클라이언트의 평균 평점 구하기
             Optional<Float> averageRating = clientReviewService.getAverageRatingForClient(project.getClientId());
             project.setAverageRating(averageRating.orElse(0.0f));  // 평점이 없을 경우 0점 설정
         }
  		List<FindFreelancerDTO> findFreelancerDTOList = findService.allFindFreelancerList(totalSearch);

  		log.debug("프로젝트: {}", projectPubloshingDTOList);
  		log.debug("프리랜서: {}", findFreelancerDTOList);
  		
  		model.addAttribute("objs", projectPubloshingDTOList);
  		model.addAttribute("partners", findFreelancerDTOList);
  		
  		return "totalSearch";
  	}  	
  	
}