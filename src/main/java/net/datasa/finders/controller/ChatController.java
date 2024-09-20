package net.datasa.finders.controller;

import net.datasa.finders.domain.dto.ChatMessageDTO;
import net.datasa.finders.domain.entity.ChatMessageEntity;
import net.datasa.finders.service.ChatMessageService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

//채팅전용
@Controller
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ChatController(ChatMessageService chatMessageService, SimpMessagingTemplate messagingTemplate) {
        this.chatMessageService = chatMessageService;
        this.messagingTemplate = messagingTemplate;
    }

    
    // 이전 채팅 메시지 로드 API
    @GetMapping("/chat/messages")
    @ResponseBody
    public List<ChatMessageDTO> getChatMessages(@RequestParam("chatroomId") int chatroomId) {
        return chatMessageService.getAllMessagesForChatroom(chatroomId);
    }
    
    @MessageMapping("/send")
    public void sendMessage(@Payload ChatMessageDTO chatMessage, 
                            @Header("simpUser") Principal principal) {
        logger.info("Received message: {}", chatMessage);

        // Principal로부터 현재 사용자 ID 설정
        String currentUserId = (principal != null) ? principal.getName() : "UnknownUser";

        // 받은 메시지에서 senderId를 현재 로그인된 사용자 ID로 설정
        chatMessage.setSenderId(currentUserId);

        try {
            // 메시지를 데이터베이스에 저장
            chatMessageService.saveMessage(chatMessage);
            logger.info("Message saved with chatroom ID: {}", chatMessage.getChatroomId());

            // 특정 채팅방으로 메시지 전송
            messagingTemplate.convertAndSend("/topic/messages/" + chatMessage.getChatroomId(), chatMessage);
            logger.info("Message sent to chatroom: /topic/messages/{}", chatMessage.getChatroomId());
        } catch (Exception e) {
            logger.error("Failed to send message to chatroom: {}", chatMessage.getChatroomId(), e);
        }
    }
    

    @GetMapping("/chat/downloadChat")
    public ResponseEntity<byte[]> downloadChat(@RequestParam("chatroomId") Long chatroomId) throws IOException {
        // 1. 데이터베이스에서 해당 채팅방의 메시지를 조회합니다.
        List<ChatMessageEntity> messageEntities = chatMessageService.getMessagesByChatroomId(chatroomId);

        // ChatMessageEntity 리스트를 ChatMessageDTO 리스트로 변환
        List<ChatMessageDTO> messages = messageEntities.stream()
                .map(entity -> ChatMessageDTO.builder()
                        .messageId(entity.getMessageId())
                        .chatroomId(entity.getChatroomId())
                        .senderId(entity.getSenderId())
                        .messageContents(entity.getMessageContents())
                        .sendTime(entity.getSendTime())
                        .build())
                .collect(Collectors.toList());

        // 2. Apache POI를 사용해 엑셀 파일을 생성합니다.
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Chat Log");

        // 헤더 작성
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("보낸 사람");
        headerRow.createCell(1).setCellValue("메시지");
        headerRow.createCell(2).setCellValue("보낸 시간");

        // 메시지 데이터를 엑셀에 삽입
        int rowNum = 1;
        for (ChatMessageDTO message : messages) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(message.getSenderId());
            row.createCell(1).setCellValue(message.getMessageContents());
            row.createCell(2).setCellValue(message.getSendTime().toString());
        }

        // 엑셀 파일을 바이트 배열로 변환
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        // 파일을 응답으로 반환
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", "chat_log.xlsx");
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());
    }
}
