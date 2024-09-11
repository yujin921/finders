package net.datasa.finders.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

// 채팅 전용 DTO
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDTO {
    private int chatroomId;       // 채팅방 ID
    private String chatroomName;  // 채팅방 이름
    private int projectNum;       // 프로젝트 번호 추가
    private String projectTitle;  // 프로젝트 제목
    private Timestamp createdTime; // 생성 시간
}
