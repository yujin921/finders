package net.datasa.finders.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @GetMapping("/room")
    public String getChatRoom(@RequestParam("id") int chatroomId, Model model) {
        System.out.println("Requested chatroom ID: " + chatroomId); // 로그 출력
        ChatRoomEntity chatRoom = chatRoomService.getChatRoomById(chatroomId);
        if (chatRoom != null) {
            String currentUserId = getCurrentUserId();
            model.addAttribute("chatroomName", chatRoom.getChatroomName());
            model.addAttribute("chatroomId", chatroomId);
            model.addAttribute("currentUserId", currentUserId);
            return "chat/chatroom"; // chatroom.html로 이동
        } else {
            return "error"; // 채팅방이 존재하지 않는 경우 에러 페이지로 이동
        }
    }


    // 현재 로그인한 사용자의 ID를 반환하는 메서드
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
            return user.getUsername(); // 사용자 ID를 반환
        }
        return "unknown"; // 로그인하지 않은 경우
    }
    
    
}
