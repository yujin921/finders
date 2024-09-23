package net.datasa.finders.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@RequiredArgsConstructor
@Controller
public class WebRTCController {
    // WebRTC HTML 페이지 제공
    @GetMapping("/webrtc")
    public String getWebCamPage() {
        return "webcam/webCam"; // templates/webcam/webCam.html 파일을 반환
    }

    @MessageMapping("/signaling") // 클라이언트가 /app/signaling으로 메시지를 보냅니다.
    @SendTo("/topic/signaling")   // 메시지가 /topic/signaling으로 브로드캐스트됩니다.
    public String signaling(String message) {
        // SDP 및 ICE 후보를 처리하는 로직
        return message;
    }
}
