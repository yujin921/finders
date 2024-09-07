package net.datasa.finders.domain.dto;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private int messageId;      // 메시지 ID (Primary Key)
    private int chatroomId;     // 채팅방 ID
    private String senderId;    // 발신자 ID
    private String messageContents; // 메시지 내용 (엔티티와 이름 일치)
    private Timestamp sendTime; // 메시지 전송 시간
}
