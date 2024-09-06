package net.datasa.finders.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.domain.dto.BoardDTO;
import net.datasa.finders.security.AuthenticatedUser;
import net.datasa.finders.service.BoardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * 거래 게시판 관련 콘트롤러
 */

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("board")
public class BoardController {
	
	private final BoardService boardService;
	
	@GetMapping("view")
	public String view() {
		return "board/list";
	}
	
	@GetMapping("write")
    public String write() {
        return "board/writeForm";
    }

    @ResponseBody
    @GetMapping("list")
    public List<BoardDTO> list(@AuthenticationPrincipal AuthenticatedUser user) {
        //서비스로 사용자 아이디를 전달하여 해당 아이디의 수입,지출 내역을 목록으로 리턴한다.
        List<BoardDTO> list = boardService.getList(user.getUsername());
        return list;
    }

    @PostMapping("write")
    public String write(
            @ModelAttribute BoardDTO boardDTO,
            @RequestParam List<String> selectedWorkScopes,
            @RequestParam List<String> selectedCategories,
            @RequestParam("projectImageFile") MultipartFile projectImageFile, // 이미지 파일
            @RequestParam("selectedSkills") String selectedSkills,  // 관련 기술
            @RequestParam("projectDescription") String projectDescription,  // 상세 업무 내용
            @AuthenticationPrincipal AuthenticatedUser user) {

        // 작성한 글에 사용자 아이디 추가
        boardDTO.setClientId(user.getUsername());
        boardDTO.setSelectedSkills(Arrays.asList(selectedSkills.split(",")));  // 콤마로 구분된 기술 리스트로 변환
        boardDTO.setProjectDescription(projectDescription);

        // BoardService 호출해서 프로젝트 및 관련 데이터 저장
        boardService.write(boardDTO, selectedWorkScopes, selectedCategories, projectImageFile);

        return "redirect:view";
    }

    /*
    @PostMapping("/upload")
    public ResponseEntity<BoardEntity> uploadBoardImage(@RequestParam("image") MultipartFile file) throws IOException {
        BoardEntity savedBoard = boardService.saveBoardImage(file);
        return ResponseEntity.ok(savedBoard);
    }

     */

	@GetMapping("read")
	public String read(@RequestParam("projectNum") int pNum, Model model, @AuthenticationPrincipal AuthenticatedUser user) {
	    try {
	        BoardDTO boardDTO = boardService.getBoard(pNum);
	        model.addAttribute("board", boardDTO);
	        model.addAttribute("user", user);
	        return "board/read";
	    } catch (Exception e) {
	        return "redirect:/board/view";
	    }
	}

	@PostMapping("delete")
    public String deletePost(@RequestParam("projectNum") int pNum, @AuthenticationPrincipal AuthenticatedUser user) {
        try {
            BoardDTO boardDTO = boardService.getBoard(pNum);

            // 로그인된 사용자와 게시글 작성자가 같은지 확인
            if (boardDTO.getClientId().equals(user.getUsername())) {
                boardService.deleteBoard(pNum);
                return "redirect:/board/view"; // 게시글 목록 페이지로 리다이렉트
            } else {
                return "redirect:/board/view?error=deletePermission"; // 권한 오류 페이지로 리다이렉트
            }
        } catch (Exception e) {
            return "redirect:/board/view?error=deleteFailed"; // 삭제 실패 페이지로 리다이렉트
        }
    }
}
