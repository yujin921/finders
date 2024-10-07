package net.datasa.finders.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.FreelancerPortfoliosDTO;
import net.datasa.finders.security.AuthenticatedUser;
import net.datasa.finders.service.FreelancerPortfoliosService;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("portfolio")
public class PortfolioController {
	
	private final FreelancerPortfoliosService freelancerPortfoliosService;

	@GetMapping("create")
    public String portfolio(Model model
    		,@AuthenticationPrincipal AuthenticatedUser user) {
		List<FreelancerPortfoliosDTO> freelancerPortfoliosDTOList = freelancerPortfoliosService.findPortfolioList(user.getId());
		
		model.addAttribute("portfoliosList", freelancerPortfoliosDTOList);
		
		
        return "/portfolio/create";
    }
	
	@PostMapping("save")
    public String save(@ModelAttribute FreelancerPortfoliosDTO FPDTO
    		, @AuthenticationPrincipal AuthenticatedUser user
    		, Model model) {
		freelancerPortfoliosService.save(FPDTO, user);
		model.addAttribute("freelancerPortfolios", FPDTO);

		List<FreelancerPortfoliosDTO> freelancerPortfoliosDTOList = freelancerPortfoliosService.findPortfolioList(user.getId());
		
		model.addAttribute("portfoliosList", freelancerPortfoliosDTOList);
        return "/portfolio/content";
    }
	
	@GetMapping("delete")
    public String delete(@RequestParam("portfolioId") int portfolioId, 
                         @AuthenticationPrincipal AuthenticatedUser user, 
                         Model model) {
        // 포트폴리오 삭제
        freelancerPortfoliosService.deletePortfolio(portfolioId, user.getId());

        // 삭제 후 남은 포트폴리오 목록 조회
        List<FreelancerPortfoliosDTO> freelancerPortfoliosDTOList = 
            freelancerPortfoliosService.findPortfolioList(user.getId());

        model.addAttribute("portfoliosList", freelancerPortfoliosDTOList);

        // 포트폴리오 목록 페이지로 리다이렉트
        return "redirect:/member/myPage";
    }
	
	@GetMapping("edit")
	public String editPortfolio(@RequestParam("portfolioId") int portfolioId
			, Model model
			, @AuthenticationPrincipal AuthenticatedUser user) throws Exception {
	    FreelancerPortfoliosDTO portfolio = freelancerPortfoliosService.getPortfolioById(portfolioId, user.getId());
	    model.addAttribute("portfolio", portfolio);
	    return "/portfolio/edit";
	}
	
	@PostMapping("update")
	public String updatePortfolio(@ModelAttribute FreelancerPortfoliosDTO updatedPortfolio,
	                              @AuthenticationPrincipal AuthenticatedUser user) throws Exception {
	    freelancerPortfoliosService.updatePortfolio(updatedPortfolio, user.getId());
	    return "redirect:/portfolio/content?portfolioId=" + updatedPortfolio.getPortfolioId();
	}
	
	// 이미지 업로드 경로 설정 (로컬 경로 예시)
	private static final String UPLOAD_DIR = "C:/upload/portfolio/";

	/*
	 * @PostMapping("/upload-image") public ResponseEntity<Map<String, Object>>
	 * uploadImage(@RequestParam("upload") MultipartFile file) {
	 * log.debug("파일:{}",file); Map<String, Object> response = new HashMap<>(); try
	 * { // 고유한 파일 이름 생성 String originalFilename = file.getOriginalFilename();
	 * String fileExtension =
	 * originalFilename.substring(originalFilename.lastIndexOf(".")); String
	 * savedFilename = UUID.randomUUID().toString() + fileExtension;
	 * 
	 * // 파일 저장 File saveFile = new File(UPLOAD_DIR + savedFilename);
	 * file.transferTo(saveFile);
	 * 
	 * // 이미지 URL 반환 String imageUrl = "/image/" + savedFilename; // 서버에서 제공할 이미지
	 * URL response.put("url", imageUrl); return new ResponseEntity<>(response,
	 * HttpStatus.OK); } catch (IOException e) { e.printStackTrace();
	 * response.put("error", "Image upload failed"); return new
	 * ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); } }
	 */
	
	/**
	 * 포트폴리오 작성시 임시로 저장되는 파일들 올라가는 장소 설정 및 에디터에 반환
	 * @param file
	 * @return
	 * @throws IOException
	 */
	@ResponseBody
    @PostMapping("/upload-image")
	public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("upload") MultipartFile file) throws IOException {
	    // 1. 파일 유효성 검사
	    if (file.isEmpty()) {
	        return ResponseEntity.badRequest().body(Map.of("error", "파일이 비어있습니다."));
	    }

	    // 2. 파일 이름 생성 (중복 방지를 위해 UUID 사용)
	    String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

	    // 3. 저장 경로 설정
	    Path uploadPath = Paths.get(UPLOAD_DIR);
	    if (!Files.exists(uploadPath)) {
	        Files.createDirectories(uploadPath);
	    }

	    // 4. 파일 저장
	    try (InputStream inputStream = file.getInputStream()) {
	        Path filePath = uploadPath.resolve(fileName);
	        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
	    } catch (IOException ioe) {
	        throw new IOException("파일 저장 중 오류 발생: " + fileName, ioe);
	    }

	    // 5. 저장된 파일의 URL 생성
	    String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
	            .path("/images/portfolio/")
	            .path(fileName)
	            .toUriString();

	    // 6. 응답 반환
	    return ResponseEntity.ok(Map.of("url", fileUrl));
	}
	
	
	@PostMapping("/delete-image")
    public ResponseEntity<Void> deleteImage(@RequestBody Map<String, String> payload) {
        String imageUrl = payload.get("url");
        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        Path filePath = Paths.get(UPLOAD_DIR, fileName);

        try {
            Files.deleteIfExists(filePath);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
	
	@GetMapping("content")
    public String content(@RequestParam("portfolioId") int portfolioId
    		,@AuthenticationPrincipal AuthenticatedUser user
    		,Model model) throws Exception {
		FreelancerPortfoliosDTO freelancerPortfoliosDTO = freelancerPortfoliosService.getPortfolioById(portfolioId, user.getId());
		List<FreelancerPortfoliosDTO> freelancerPortfoliosDTOList = freelancerPortfoliosService.findPortfolioList(user.getId());
		
		model.addAttribute("portfoliosList", freelancerPortfoliosDTOList);
		
		model.addAttribute("freelancerPortfolios", freelancerPortfoliosDTO);
        return "portfolio/content";
    }
    
}
