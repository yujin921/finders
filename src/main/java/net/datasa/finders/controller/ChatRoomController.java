package net.datasa.finders.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import net.datasa.finders.domain.entity.ChatRoomEntity;
import net.datasa.finders.service.ChatRoomService;

@Controller
@RequestMapping("chat")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @Autowired
    public ChatRoomController(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }

    // 채팅방 목록 페이지
    @GetMapping("list")
    public String showChatRooms(Model model) {
        List<ChatRoomEntity> chatRooms = chatRoomService.getAllChatRooms(); // 모든 채팅방 목록 가져오기
        model.addAttribute("chatrooms", chatRooms);
        return "chat/chatrooms"; // chatrooms.html로 이동
    }

    // 채팅방 생성 페이지
    @GetMapping("/create")
    public String createChatRoomPage() {
        return "create-chatroom"; // create-chatroom.html로 이동
    }

    // member_id에 기반해 채팅방 생성
    @PostMapping("/create")
    public ResponseEntity<String> createChatRoomsForMember(@RequestParam("memberId") String memberId) {
        chatRoomService.createChatRoomsForAllMemberProjects(memberId);
        return ResponseEntity.ok("채팅방이 성공적으로 생성되었습니다.");
    }
}
