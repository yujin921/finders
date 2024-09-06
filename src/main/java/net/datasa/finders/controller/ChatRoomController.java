package net.datasa.finders.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.datasa.finders.domain.dto.ChatRoomDTO;
import net.datasa.finders.service.ChatRoomService;
import net.datasa.finders.service.ProjectService;

@RestController
@RequestMapping("/chat")
public class ChatRoomController {

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private ProjectService projectService;

    // 프로젝트 번호로 채팅방 조회 (존재하지 않으면 자동 생성)
    @GetMapping("/view/{projectNum}")
    public ChatRoomDTO viewChatRoom(
            @PathVariable("projectNum") int projectNum,
            @RequestParam(value = "userId", required = false) String userId) {

        // userId가 전달되지 않은 경우 세션에서 가져오기
        if (userId == null || userId.isEmpty()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            userId = authentication.getName();  // 인증된 사용자 ID 가져오기
            System.out.println("세션에서 가져온 User ID: " + userId);
        } else {
            System.out.println("Received User ID from request: " + userId);
        }

        // 프로젝트 번호 확인
        System.out.println("Received projectNum: " + projectNum);

        // 사용자 인증 및 프로젝트 매칭 로직 추가
        if (!projectService.isUserAssignedToProject(userId, projectNum)) {
            System.out.println("User " + userId + " is not assigned to project " + projectNum);
            // 사용자 추가 로직 실행 (필요 시 주석 해제)
            // projectService.addMemberToProject(userId, projectNum);
            throw new IllegalArgumentException("User is not assigned to the project.");
        }

        // ChatRoomService 호출로 채팅방 생성 또는 조회
        return chatRoomService.getOrCreateChatRoomByProjectNum(projectNum, userId);
    }
}
