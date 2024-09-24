package net.datasa.finders.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.finders.repository.TeamRepository;
import net.datasa.finders.service.TeamService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.nio.file.AccessDeniedException;
import java.security.Principal;

@Slf4j
@RequiredArgsConstructor
@Controller
public class WebRTCController {
    private final TeamService teamService;
    private final TeamRepository teamRepository;
    // WebRTC HTML 페이지 제공
    @GetMapping("/webrtc")
    public String getWebCamPage() {
        return "webcam/webCam"; // templates/webcam/webCam.html 파일을 반환
    }

    //offer 정보를 주고 받기 위한 websocket
    //camKey : 각 요청하는 캠의 key , roomId : 룸 아이디
    @MessageMapping("/peer/offer/{camKey}/{roomId}")
    @SendTo("/topic/peer/offer/{camKey}/{roomId}")
    public String PeerHandleOffer(@Payload String offer, @DestinationVariable(value = "roomId") String roomId,
                                  @DestinationVariable(value = "camKey") String camKey,
                                  Principal principal) throws AccessDeniedException {
        String userId = principal.getName();  // 로그인한 사용자 ID 가져오기

        // roomId를 projectNum으로 간주하고, 이를 통해 팀원 여부 확인
        int projectNum = Integer.parseInt(roomId);  // roomId를 projectNum으로 사용
        log.debug("Checking if user {} is a team member of project {}", userId, projectNum);

        // 팀원 확인 로직
        boolean isTeamMember = teamService.isTeamMember(projectNum, userId);
        log.debug("User: {}, Project: {}, isTeamMember: {}", userId, projectNum, isTeamMember);
        // 팀원이 아닐 경우 예외 처리 (팀에 속하지 않은 사용자 차단)
        if (!isTeamMember) {
            log.error("User {} does not have permission to access room {} (ProjectNum: {})", userId, roomId, projectNum);
            throw new AccessDeniedException("접근 권한이 없습니다. You are not part of this project.");
        }

        log.info("[OFFER] {} : {}, User: {}, ProjectNum: {}", camKey, offer, userId, projectNum);
        return offer;
    }

    //iceCandidate 정보를 주고 받기 위한 webSocket
    //camKey : 각 요청하는 캠의 key , roomId : 룸 아이디
    @MessageMapping("/peer/iceCandidate/{camKey}/{roomId}")
    @SendTo("/topic/peer/iceCandidate/{camKey}/{roomId}")
    public String PeerHandleIceCandidate(@Payload String candidate, @DestinationVariable(value = "roomId") String roomId,
                                         @DestinationVariable(value = "camKey") String camKey) {
        log.info("[ICECANDIDATE] {} : {}", camKey, candidate);
        return candidate;
    }

    //

    @MessageMapping("/peer/answer/{camKey}/{roomId}")
    @SendTo("/topic/peer/answer/{camKey}/{roomId}")
    public String PeerHandleAnswer(@Payload String answer, @DestinationVariable(value = "roomId") String roomId,
                                   @DestinationVariable(value = "camKey") String camKey) {
        log.info("[ANSWER] {} : {}", camKey, answer);
        return answer;
    }

    //camKey 를 받기위해 신호를 보내는 webSocket
    @MessageMapping("/call/key")
    @SendTo("/topic/call/key")
    public String callKey(@Payload String message) {
        log.info("[Key] : {}", message);
        return message;
    }

    //자신의 camKey 를 모든 연결된 세션에 보내는 webSocket
    @MessageMapping("/send/key")
    @SendTo("/topic/send/key")
    public String sendKey(@Payload String message) {
        return message;
    }
}
