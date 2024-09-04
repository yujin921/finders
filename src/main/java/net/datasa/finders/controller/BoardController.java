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
	
	@PostMapping("write")
    public String write(
            @ModelAttribute BoardDTO boardDTO
            , @AuthenticationPrincipal AuthenticatedUser user) {

        //작성한 글에 사용자 아이디 추가
        boardDTO.setClientId(user.getUsername());
        log.debug("저장할 글 정보 : {}", boardDTO);
        
        boardService.write(boardDTO);
        return "redirect:view";
    }
	
	
	@GetMapping("read")
	public String read(@RequestParam("boardNum") int boardNum, Model model, @AuthenticationPrincipal AuthenticatedUser user) {
	    try {
	        BoardDTO boardDTO = boardService.getBoard(boardNum);
	        model.addAttribute("board", boardDTO);
	        model.addAttribute("user", user);
	        return "board/read";
	    } catch (Exception e) {
	        return "redirect:/board/view";
	    }
	}
	
	@PostMapping("delete")
    public String deletePost(@RequestParam("boardNum") int boardNum, @AuthenticationPrincipal AuthenticatedUser user) {
        try {
            BoardDTO boardDTO = boardService.getBoard(boardNum);

            // 로그인된 사용자와 게시글 작성자가 같은지 확인
            if (boardDTO.getClientId().equals(user.getUsername())) {
                boardService.deleteBoard(boardNum);
                return "redirect:/board/view"; // 게시글 목록 페이지로 리다이렉트
            } else {
                return "redirect:/board/view?error=deletePermission"; // 권한 오류 페이지로 리다이렉트
            }
        } catch (Exception e) {
            return "redirect:/board/view?error=deleteFailed"; // 삭제 실패 페이지로 리다이렉트
        }
    }
}
