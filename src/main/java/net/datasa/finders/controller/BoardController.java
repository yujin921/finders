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
            @RequestParam List<String> workScopes,  // 복수 카테고리 선택
            @RequestParam List<String> skills,  // 복수 기술 선택
            @AuthenticationPrincipal AuthenticatedUser user) {

        // 작성한 글에 사용자 아이디 추가
        boardDTO.setClientId(user.getUsername());
        log.debug("저장할 글 정보 : {}", boardDTO);

        // 서비스 호출: 프로젝트 제목 저장, 선택된 카테고리와 기술을 저장
        boardService.write(boardDTO, workScopes, skills);

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
