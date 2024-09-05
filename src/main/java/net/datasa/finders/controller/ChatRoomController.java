package net.datasa.finders.controller;

import net.datasa.finders.domain.dto.ChatRoomDTO;
import net.datasa.finders.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
public class ChatRoomController {

    @Autowired
    private ChatRoomService chatRoomService;

    // 프로젝트 번호로 채팅방 조회 (존재하지 않으면 자동 생성)
    @GetMapping("/view/{projectNum}")
    public ChatRoomDTO viewChatRoom(@PathVariable("projectNum") int projectNum) {
        return chatRoomService.getOrCreateChatRoomByProjectNum(projectNum);
    }
}
